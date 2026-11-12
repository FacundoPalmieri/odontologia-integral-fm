package com.odontologiaintegralfm.feature.consultation.prestation.repository;

import com.odontologiaintegralfm.feature.consultation.prestation.enums.PrestationScopeType;
import com.odontologiaintegralfm.feature.consultation.prestation.model.PrestationType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@Tag("integration")
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestJpaAuditingConfig.class)
class IPrestationTypeRepositoryTest {

    private static final long AUDIT_USER_ID = 4L;
    private static final String TREATMENT_SUFFIX = "-Test";

    @Autowired
    private IPrestationTypeRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void checkCatalogColumns() {
        // Falla en vez de saltear: un "skipped" en fase roja se lee como "no falla".
        Integer columns = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND LOWER(table_name) IN ('prestations_types', 'prestations_types_aud')
                  AND column_name = 'can_be_origin'
                """, Integer.class);
        assertThat(columns)
                .as("La base de test necesita la columna can_be_origin en prestations_types y en prestations_types_AUD (la crea la API al levantar)")
                .isEqualTo(2);
    }

    @AfterEach
    void cleanup() {
        String testPrestations = "(SELECT id FROM prestations_types WHERE name LIKE '%-Test')";
        String testTreatments = "(SELECT id FROM treatment WHERE name LIKE '%-Test')";
        jdbcTemplate.update("DELETE FROM prestation_types_dentist_specialties WHERE prestacion_id IN " + testPrestations);
        jdbcTemplate.update("DELETE FROM prestation_type_scopes WHERE prestation_type_id IN " + testPrestations);
        jdbcTemplate.update("DELETE FROM prestacion_treatment WHERE prestacion_id IN " + testPrestations);
        jdbcTemplate.update("DELETE FROM prestations_types WHERE name LIKE '%-Test'");
        jdbcTemplate.update("DELETE FROM treatment_conditions WHERE treatment_id IN " + testTreatments);
        jdbcTemplate.update("DELETE FROM treatments_dentist_specialties WHERE treatment_id IN " + testTreatments);
        jdbcTemplate.update("DELETE FROM treatment WHERE name LIKE '%-Test'");
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Long insertTreatment(String name) {
        return insertTreatment(name, true);
    }

    private Long insertTreatment(String name, boolean enabled) {
        jdbcTemplate.update("""
                INSERT INTO treatment (label, name, enabled, created_at, created_by_id)
                VALUES (?, ?, ?, NOW(), ?)
                """, name, name, enabled, AUDIT_USER_ID);
        return jdbcTemplate.queryForObject("SELECT id FROM treatment WHERE name = ?", Long.class, name);
    }

    /** Prestación "-Test" con fecha de creación explícita: las filas del árbol (REQ-039) dependen del día de created_at. */
    private Long insertPrestationType(String name, boolean enabled, LocalDateTime createdAt) {
        jdbcTemplate.update("""
                INSERT INTO prestations_types (name, label, is_unique, requires_origin_treatment, can_be_origin, enabled, created_at, created_by_id)
                VALUES (?, ?, false, false, false, ?, ?, ?)
                """, name, name, enabled, createdAt, AUDIT_USER_ID);
        return jdbcTemplate.queryForObject("SELECT id FROM prestations_types WHERE name = ?", Long.class, name);
    }

    private Long specialtyId(String name) {
        List<Long> ids = jdbcTemplate.queryForList("SELECT id FROM dental_specialties WHERE name = ?", Long.class, name);
        assertThat(ids)
                .as("La base de test necesita la especialidad '" + name + "' del script de altas iniciales")
                .hasSize(1);
        return ids.get(0);
    }

    private void addSpecialty(Long prestationTypeId, Long specialtyId) {
        jdbcTemplate.update("INSERT INTO prestation_types_dentist_specialties (prestacion_id, dentist_specialty_id) VALUES (?, ?)",
                prestationTypeId, specialtyId);
    }

    private void addScope(Long prestationTypeId, PrestationScopeType scope) {
        jdbcTemplate.update("INSERT INTO prestation_type_scopes (prestation_type_id, scope_type) VALUES (?, ?)",
                prestationTypeId, scope.name());
    }

    private static List<Long> ids(List<PrestationType> prestations) {
        return prestations.stream().map(PrestationType::getId).toList();
    }

    private Long insertPrestationType(String name, boolean requiresOrigin, boolean enabled) {
        jdbcTemplate.update("""
                INSERT INTO prestations_types (name, label, is_unique, requires_origin_treatment, can_be_origin, enabled, created_at, created_by_id)
                VALUES (?, ?, false, ?, false, ?, NOW(), ?)
                """, name, name, requiresOrigin, enabled, AUDIT_USER_ID);
        return jdbcTemplate.queryForObject("SELECT id FROM prestations_types WHERE name = ?", Long.class, name);
    }

    private void link(Long prestationTypeId, Long treatmentId) {
        jdbcTemplate.update("INSERT INTO prestacion_treatment (prestacion_id, treatment_id) VALUES (?, ?)",
                prestationTypeId, treatmentId);
    }

    // ─── Tests ────────────────────────────────────────────────────────────────

    /**
     * CASO: el tratamiento A se relaciona con dos tipos requires_origin_treatment = true y enabled = true;
     * el tratamiento B solo con un tipo requires_origin_treatment = false.
     * Regla: D-02 — se leen los ids de Treatment ligados a arreglos de prótesis, sin repetir.
     * Validación: el resultado contiene el id de A exactamente una vez y no contiene el id de B.
     */
    @Test
    void findTreatmentIdsLinkedToOriginRepairs_originAndNonOriginTypes_returnsOnlyOriginLinkedOnce() {
        Long treatmentA = insertTreatment("TratamientoA" + TREATMENT_SUFFIX);
        Long treatmentB = insertTreatment("TratamientoB" + TREATMENT_SUFFIX);
        link(insertPrestationType("Arreglo1-Test", true, true), treatmentA);
        link(insertPrestationType("Arreglo2-Test", true, true), treatmentA);
        link(insertPrestationType("Colocacion-Test", false, true), treatmentB);

        List<Long> result = repository.findTreatmentIdsLinkedToOriginRepairs();

        assertThat(Collections.frequency(result, treatmentA)).isEqualTo(1);
        assertThat(result).doesNotContain(treatmentB);
    }

    /**
     * CASO: el tratamiento C se relaciona solo con un tipo requires_origin_treatment = true y enabled = false.
     * Regla: D-02 — un tipo de arreglo dado de baja no hace que su tratamiento cuente como "de prótesis".
     * Validación: el resultado no contiene el id de C.
     */
    @Test
    void findTreatmentIdsLinkedToOriginRepairs_disabledOriginType_notReturned() {
        Long treatmentC = insertTreatment("TratamientoC" + TREATMENT_SUFFIX);
        link(insertPrestationType("ArregloBaja-Test", true, false), treatmentC);

        List<Long> result = repository.findTreatmentIdsLinkedToOriginRepairs();

        assertThat(result).doesNotContain(treatmentC);
    }

    // ─── REQ-039: ramas del árbol clínico ─────────────────────────────────────
    // Las fechas son fijas (no "hoy"): la fecha de creación de cada fila se declara respecto de ASOF y así el resultado no
    // depende del reloj ni de la zona horaria de la base.

    private static final LocalDate ASOF = LocalDate.of(2026, 11, 9);

    /**
     * CASO: "Sola-Test" está habilitada, no tiene ninguna fila en prestacion_treatment y tiene Odontopediatría en sus especialidades.
     * Regla: D-01 y D-02 — la rama 2 trae las prestaciones sin tratamiento de la especialidad pedida.
     * Validación: pidiendo Odontopediatría y la fecha ASOF, el id está en la lista.
     */
    @Test
    void findWithoutTreatmentBySpecialty_ownSpecialtyNoTreatment_isReturned() {
        Long pediatric = specialtyId("Odontopediatria");
        Long prestation = insertPrestationType("Sola-Test", true, ASOF.atTime(10, 0));
        addSpecialty(prestation, pediatric);

        List<PrestationType> result = repository.findWithoutTreatmentBySpecialty(pediatric, ASOF);

        assertThat(ids(result)).contains(prestation);
    }

    /**
     * CASO: "ConTratamiento-Test" tiene una fila en prestacion_treatment (hacia un tratamiento habilitado) y Odontopediatría en sus especialidades.
     * Regla: D-01 — una prestación que cuelga de un tratamiento no sale en la rama 2, aunque tenga la especialidad.
     * Validación: pidiendo Odontopediatría y ASOF, el id no está en la lista.
     */
    @Test
    void findWithoutTreatmentBySpecialty_hasTreatment_notReturned() {
        Long pediatric = specialtyId("Odontopediatria");
        Long prestation = insertPrestationType("ConTratamiento-Test", true, ASOF.atTime(10, 0));
        addSpecialty(prestation, pediatric);
        link(prestation, insertTreatment("Tratamiento-Test"));

        List<PrestationType> result = repository.findWithoutTreatmentBySpecialty(pediatric, ASOF);

        assertThat(ids(result)).doesNotContain(prestation);
    }

    /**
     * CASO: "Ortodoncia-Test" es solo de Ortodoncia y "SinEspecialidad-Test" no tiene ninguna especialidad; las dos sin tratamiento.
     * Regla: D-02 y D-12 — otra especialidad, o ninguna, no sale (el catálogo vacío no es "para todos").
     * Validación: pidiendo Odontopediatría y ASOF, ninguno de los dos ids está en la lista.
     */
    @Test
    void findWithoutTreatmentBySpecialty_otherSpecialtyOrNone_notReturned() {
        Long pediatric = specialtyId("Odontopediatria");
        Long orthodontics = specialtyId("Ortodoncia");
        Long orthodonticsOnly = insertPrestationType("Ortodoncia-Test", true, ASOF.atTime(10, 0));
        addSpecialty(orthodonticsOnly, orthodontics);
        Long withoutSpecialties = insertPrestationType("SinEspecialidad-Test", true, ASOF.atTime(10, 0));

        List<PrestationType> result = repository.findWithoutTreatmentBySpecialty(pediatric, ASOF);

        assertThat(ids(result)).doesNotContain(orthodonticsOnly, withoutSpecialties);
    }

    /**
     * CASO: "TardeDelDia-Test" (sin tratamiento, de Odontopediatría) se creó el día ASOF a las 23:59.
     * Regla: D-04 — la fecha pedida está incluida: se compara el día, no la hora.
     * Validación: pidiendo ASOF, el id está en la lista.
     */
    @Test
    void findWithoutTreatmentBySpecialty_createdLateOnDate_isIncluded() {
        Long pediatric = specialtyId("Odontopediatria");
        Long prestation = insertPrestationType("TardeDelDia-Test", true, ASOF.atTime(23, 59));
        addSpecialty(prestation, pediatric);

        List<PrestationType> result = repository.findWithoutTreatmentBySpecialty(pediatric, ASOF);

        assertThat(ids(result)).contains(prestation);
    }

    /**
     * CASO: "DiaSiguiente-Test" (sin tratamiento, de Odontopediatría) se creó el día siguiente a ASOF a las 00:00.
     * Regla: D-04 — las prestaciones creadas después de la fecha no existían cuando se hizo la atención.
     * Validación: pidiendo ASOF, el id no está en la lista.
     */
    @Test
    void findWithoutTreatmentBySpecialty_createdAfterDate_notReturned() {
        Long pediatric = specialtyId("Odontopediatria");
        Long prestation = insertPrestationType("DiaSiguiente-Test", true, ASOF.plusDays(1).atStartOfDay());
        addSpecialty(prestation, pediatric);

        List<PrestationType> result = repository.findWithoutTreatmentBySpecialty(pediatric, ASOF);

        assertThat(ids(result)).doesNotContain(prestation);
    }

    /**
     * CASO: "Baja-Test" (sin tratamiento, de Odontopediatría) está dada de baja (enabled = false).
     * Regla: el catálogo solo muestra prestaciones habilitadas.
     * Validación: pidiendo Odontopediatría y ASOF, el id no está en la lista.
     */
    @Test
    void findWithoutTreatmentBySpecialty_disabledPrestation_notReturned() {
        Long pediatric = specialtyId("Odontopediatria");
        Long prestation = insertPrestationType("Baja-Test", false, ASOF.atTime(10, 0));
        addSpecialty(prestation, pediatric);

        List<PrestationType> result = repository.findWithoutTreatmentBySpecialty(pediatric, ASOF);

        assertThat(ids(result)).doesNotContain(prestation);
    }

    /**
     * CASO: "Radiografia-Test" cuelga de los tratamientos A, B y C ("-Test"); se piden los ids de A y B.
     * Regla: D-03 — un par (tratamiento, prestación) por cada tratamiento pedido; la prestación se repite bajo cada uno.
     * Validación: la lista tiene exactamente dos pares, (A, prestación) y (B, prestación), y cada par trae el PrestationType
     * de esa prestación; el par del tratamiento C no aparece.
     */
    @Test
    void findTreatmentLinks_returnsOnePairPerTreatmentOfRequestedIds() {
        Long prestation = insertPrestationType("Radiografia-Test", true, ASOF.atTime(10, 0));
        Long treatmentA = insertTreatment("TratamientoA" + TREATMENT_SUFFIX);
        Long treatmentB = insertTreatment("TratamientoB" + TREATMENT_SUFFIX);
        Long treatmentC = insertTreatment("TratamientoC" + TREATMENT_SUFFIX);
        link(prestation, treatmentA);
        link(prestation, treatmentB);
        link(prestation, treatmentC);

        List<TreatmentPrestationTypeLink> result = repository.findTreatmentLinks(List.of(treatmentA, treatmentB), ASOF);

        assertThat(result)
                .extracting(TreatmentPrestationTypeLink::treatmentId, pair -> pair.prestationType().getId())
                .containsExactlyInAnyOrder(tuple(treatmentA, prestation), tuple(treatmentB, prestation));
    }

    /**
     * CASO: el tratamiento A tiene dos prestaciones: "DiaSiguiente-Test" (creada el día siguiente a ASOF) y "Baja-Test" (enabled = false).
     * Regla: D-04 — los pares solo incluyen prestaciones habilitadas y creadas hasta la fecha pedida.
     * Validación: pidiendo A y ASOF, la lista está vacía.
     */
    @Test
    void findTreatmentLinks_createdAfterDateOrDisabled_notReturned() {
        Long treatmentA = insertTreatment("TratamientoA" + TREATMENT_SUFFIX);
        link(insertPrestationType("DiaSiguiente-Test", true, ASOF.plusDays(1).atStartOfDay()), treatmentA);
        link(insertPrestationType("Baja-Test", false, ASOF.atTime(10, 0)), treatmentA);

        List<TreatmentPrestationTypeLink> result = repository.findTreatmentLinks(List.of(treatmentA), ASOF);

        assertThat(result).isEmpty();
    }

    /**
     * CASO: P1 ("ConScope-Test") tiene un scope, P2 ("SinScope-Test") ninguno y P3 ("OtraConScope-Test") tiene uno pero no se pide.
     * Regla: D-01 — los allowedScopes se cargan en un lote, y una prestación sin scopes (como las de prótesis de origen) sale igual.
     * Validación: pidiendo los ids de P1 y P2, la lista contiene exactamente P1 y P2, una vez cada una.
     */
    @Test
    void findAllWithAllowedScopesByIdIn_prestationWithoutScopes_isIncluded() {
        Long withScope = insertPrestationType("ConScope-Test", true, ASOF.atTime(10, 0));
        addScope(withScope, PrestationScopeType.TOOTH);
        Long withoutScope = insertPrestationType("SinScope-Test", true, ASOF.atTime(10, 0));
        Long notRequested = insertPrestationType("OtraConScope-Test", true, ASOF.atTime(10, 0));
        addScope(notRequested, PrestationScopeType.QUADRANT);

        List<PrestationType> result = repository.findAllWithAllowedScopesByIdIn(List.of(withScope, withoutScope));

        assertThat(ids(result)).containsExactlyInAnyOrder(withScope, withoutScope);
    }
}