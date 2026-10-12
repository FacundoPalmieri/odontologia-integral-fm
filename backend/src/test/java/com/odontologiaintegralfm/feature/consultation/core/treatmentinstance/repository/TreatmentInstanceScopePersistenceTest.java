package com.odontologiaintegralfm.feature.consultation.core.treatmentinstance.repository;

import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.model.PrestationType;
import com.odontologiaintegralfm.feature.consultation.catalogs.treatment.enums.TreatmentConditionType;
import com.odontologiaintegralfm.feature.consultation.catalogs.treatment.model.Treatment;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.enums.PrestationInstanceStatus;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.model.PrestationInstance;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.repository.IPrestationInstanceRepository;
import com.odontologiaintegralfm.feature.consultation.core.scope.enums.Maxillary;
import com.odontologiaintegralfm.feature.consultation.core.scope.enums.Quadrant;
import com.odontologiaintegralfm.feature.consultation.core.scope.enums.Tooth;
import com.odontologiaintegralfm.feature.consultation.core.scope.enums.ToothFace;
import com.odontologiaintegralfm.feature.consultation.core.scope.model.Scope;
import com.odontologiaintegralfm.feature.consultation.core.treatmentinstance.model.TreatmentInstance;
import com.odontologiaintegralfm.feature.patient.core.model.Patient;
import com.odontologiaintegralfm.feature.user.model.UserSec;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Persistencia del embeddable Scope y de las queries por paciente de TreatmentInstance y PrestationInstance.
 * Requiere el script v.27 aplicado en la base del perfil test (ddl-auto=validate).
 * Los pacientes "Alan" y "Bety" los inserta el propio test (persons + patients por JDBC, con DNI propios) y los borra al
 * terminar: no depende de qué pacientes tenga la base ni asume que estén vacíos. Las filas de referencia (tipo de DNI,
 * género y nacionalidad con id 1) vienen del script de altas iniciales; si faltan, los tests fallan con un mensaje claro
 * en vez de saltearse.
 */
@Tag("integration")
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TreatmentInstanceScopePersistenceTest.AuditingConfig.class)
class TreatmentInstanceScopePersistenceTest {

    /**
     * TestJpaAuditingConfig del paquete catalogs.prestation.repository es package-private y devuelve auditor vacío;
     * acá el auditor es el usuario 4 (el mismo que usan los INSERT por JDBC) porque created_by es obligatorio en la base.
     */
    @TestConfiguration
    @EnableJpaAuditing(auditorAwareRef = "treatmentInstanceTestAuditor")
    static class AuditingConfig {
        @Bean("treatmentInstanceTestAuditor")
        AuditorAware<UserSec> treatmentInstanceTestAuditor(EntityManager entityManager) {
            return () -> Optional.ofNullable(entityManager.find(UserSec.class, AUDIT_USER_ID));
        }
    }

    private static final long AUDIT_USER_ID = 4L;
    private static final String TREATMENT_NAME = "Tratamiento-Test";
    private static final String TYPE_NAME = "Prestacion-Test";
    private static final String ALAN_DNI = "TST-SCOPE-ALAN";
    private static final String BETY_DNI = "TST-SCOPE-BETY";

    @Autowired private EntityManager em;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private ITreatmentInstanceRepository treatmentInstanceRepository;
    @Autowired private IPrestationInstanceRepository prestationInstanceRepository;

    private Patient alan;
    private Patient bety;
    private Treatment treatment;
    private PrestationType prestationType;

    @BeforeEach
    void setUp() {
        // Falla en vez de saltear: un "skipped" en fase roja se lee como "no falla".
        assertThat(jdbcTemplate.queryForObject(
                "SELECT (SELECT COUNT(*) FROM dni_types WHERE id = 1) + (SELECT COUNT(*) FROM genders WHERE id = 1)"
                        + " + (SELECT COUNT(*) FROM nationalities WHERE id = 1)", Integer.class))
                .as("La base de test necesita las filas de referencia id 1 de dni_types, genders y nationalities (script de altas iniciales)")
                .isEqualTo(3);
        Long alanId = insertPatient("Alan", ALAN_DNI);
        Long betyId = insertPatient("Bety", BETY_DNI);

        jdbcTemplate.update("""
                INSERT INTO treatment (label, name, enabled, created_at, created_by_id)
                VALUES (?, ?, true, NOW(), ?)
                """, TREATMENT_NAME, TREATMENT_NAME, AUDIT_USER_ID);
        jdbcTemplate.update("""
                INSERT INTO prestations_types (name, label, is_unique, requires_origin_treatment, enabled, created_at, created_by_id)
                VALUES (?, ?, false, false, true, NOW(), ?)
                """, TYPE_NAME, TYPE_NAME, AUDIT_USER_ID);

        Long treatmentId = jdbcTemplate.queryForObject("SELECT id FROM treatment WHERE name = ?", Long.class, TREATMENT_NAME);
        Long typeId = jdbcTemplate.queryForObject("SELECT id FROM prestations_types WHERE name = ?", Long.class, TYPE_NAME);

        alan = em.find(Patient.class, alanId);
        bety = em.find(Patient.class, betyId);
        assertThat(alan).as("El paciente Alan insertado por el test debe poder leerse").isNotNull();
        assertThat(bety).as("El paciente Bety insertado por el test debe poder leerse").isNotNull();
        treatment = em.find(Treatment.class, treatmentId);
        prestationType = em.find(PrestationType.class, typeId);
    }

    @AfterEach
    void cleanup() {
        em.clear();
        jdbcTemplate.update("DELETE FROM prestations_instances_tooth WHERE prestation_instance_id IN " +
                "(SELECT id FROM prestations_instances WHERE prestation_type_id IN (SELECT id FROM prestations_types WHERE name = ?))", TYPE_NAME);
        jdbcTemplate.update("DELETE FROM prestations_instances WHERE prestation_type_id IN " +
                "(SELECT id FROM prestations_types WHERE name = ?)", TYPE_NAME);
        jdbcTemplate.update("DELETE FROM treatments_instances_tooth WHERE treatment_instance_id IN " +
                "(SELECT id FROM treatments_instances WHERE treatment_id IN (SELECT id FROM treatment WHERE name = ?))", TREATMENT_NAME);
        jdbcTemplate.update("DELETE FROM treatments_instances WHERE treatment_id IN " +
                "(SELECT id FROM treatment WHERE name = ?)", TREATMENT_NAME);
        jdbcTemplate.update("DELETE FROM prestations_types WHERE name = ?", TYPE_NAME);
        jdbcTemplate.update("DELETE FROM treatment WHERE name = ?", TREATMENT_NAME);
        jdbcTemplate.update("DELETE FROM patients WHERE person_id IN (SELECT id FROM persons WHERE dni IN (?, ?))", ALAN_DNI, BETY_DNI);
        jdbcTemplate.update("DELETE FROM persons WHERE dni IN (?, ?)", ALAN_DNI, BETY_DNI);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /** Inserta una persona y su paciente (el id del paciente es el de la persona, @MapsId) y devuelve ese id. */
    private Long insertPatient(String firstName, String dni) {
        jdbcTemplate.update("""
                INSERT INTO persons (first_name, last_name, dni_type_id, dni, birth_date, gender_id, nationality_id, enabled, created_at, created_by_id)
                VALUES (?, 'ScopeTest', 1, ?, '1990-01-01', 1, 1, true, NOW(), ?)
                """, firstName, dni, AUDIT_USER_ID);
        Long personId = jdbcTemplate.queryForObject("SELECT id FROM persons WHERE dni = ?", Long.class, dni);
        jdbcTemplate.update("INSERT INTO patients (person_id, enabled, created_at, created_by_id) VALUES (?, true, NOW(), ?)",
                personId, AUDIT_USER_ID);
        return personId;
    }

    private TreatmentInstance persistTreatmentInstance(Patient patient, Scope scope, TreatmentConditionType condition) {
        TreatmentInstance treatmentInstance = TreatmentInstance.build(patient, scope, treatment, condition);
        em.persist(treatmentInstance);
        return treatmentInstance;
    }

    private PrestationInstance persistPrestationInstance(Patient patient, TreatmentInstance treatmentInstance,
                                                         Scope ownScope, PrestationInstanceStatus status) {
        PrestationInstance prestationInstance = PrestationInstance.build(patient, prestationType, treatmentInstance, null,
                ownScope, status, new BigDecimal("1000.00"), null, null, null, null);
        em.persist(prestationInstance);
        return prestationInstance;
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    private static Scope toothScope(Tooth... teeth) {
        return Scope.build(List.of(teeth), null, null, null, false);
    }

    // ─── Scope embebido ───────────────────────────────────────────────────────

    /**
     * CASO: TreatmentInstance de Bety con scope tooth [16, 17] y ningún otro campo, tras flush + clear.
     * Regla: decisión 2 — fullMouth no nulo evita que Hibernate hidrate el embeddable en null y se pierda la lista.
     * Validación: al recargar, scope no es null, tooth contiene 16 y 17 sin garantía de orden (la colección no tiene
     * @OrderColumn y el orden no es regla de negocio) y fullMouth es false.
     */
    @Test
    void treatmentInstance_persistWithToothList_reloadKeepsToothList() {
        Long id = persistTreatmentInstance(bety, toothScope(Tooth.T16, Tooth.T17), TreatmentConditionType.REQUIRED).getId();
        flushAndClear();

        Scope reloaded = em.find(TreatmentInstance.class, id).getScope();

        assertThat(reloaded).isNotNull();
        assertThat(reloaded.getTooth()).containsExactlyInAnyOrder(Tooth.T16, Tooth.T17);
        assertThat(reloaded.isFullMouth()).isFalse();
    }

    /**
     * CASO: TreatmentInstance con scope quadrant = UPPER_RIGHT.
     * Regla: un nivel geométrico se persiste tal cual, sin expandir a dientes (D-07).
     * Validación: quadrant = UPPER_RIGHT y tooth vacío (no null) al recargar.
     */
    @Test
    void treatmentInstance_persistWithQuadrantOnly_reloadKeepsQuadrantAndEmptyTooth() {
        Scope quadrantScope = Scope.build(List.of(), null, Quadrant.UPPER_RIGHT, null, false);
        Long id = persistTreatmentInstance(bety, quadrantScope, TreatmentConditionType.REQUIRED).getId();
        flushAndClear();

        Scope reloaded = em.find(TreatmentInstance.class, id).getScope();

        assertThat(reloaded.getQuadrant()).isEqualTo(Quadrant.UPPER_RIGHT);
        assertThat(reloaded.getTooth()).isNotNull().isEmpty();
    }

    /**
     * CASO: TreatmentInstance con scope fullMouth = true.
     * Regla: D-05 — boca completa se persiste como el flag, sin otros campos.
     * Validación: fullMouth = true y quadrant, maxillary, toothFace nulos y tooth vacío.
     */
    @Test
    void treatmentInstance_persistFullMouth_reloadKeepsFullMouth() {
        Scope fullMouth = Scope.build(List.of(), null, null, null, true);
        Long id = persistTreatmentInstance(bety, fullMouth, TreatmentConditionType.REQUIRED).getId();
        flushAndClear();

        Scope reloaded = em.find(TreatmentInstance.class, id).getScope();

        assertThat(reloaded.isFullMouth()).isTrue();
        assertThat(reloaded.getQuadrant()).isNull();
        assertThat(reloaded.getMaxillary()).isNull();
        assertThat(reloaded.getToothFace()).isNull();
        assertThat(reloaded.getTooth()).isEmpty();
    }

    /**
     * CASO: Prestación con scope propio maxillary = UPPER sin tratamiento; y otra con tooth [24] y toothFace TOP.
     * Regla: la prestación tiene su propia tabla de dientes (prestations_instances_tooth), distinta de la del tratamiento.
     * Validación: el scope recargado coincide en ambos casos y el diente 24 está en la tabla propia de la prestación.
     */
    @Test
    void prestationInstance_persistWithOwnScope_reloadKeepsScope() {
        Long maxillaryId = persistPrestationInstance(bety, null,
                Scope.build(List.of(), null, null, Maxillary.UPPER, false), PrestationInstanceStatus.IN_PROGRESS).getId();
        Long toothId = persistPrestationInstance(bety, null,
                Scope.build(List.of(Tooth.T24), ToothFace.TOP, null, null, false), PrestationInstanceStatus.IN_PROGRESS).getId();
        flushAndClear();

        Scope maxillaryScope = em.find(PrestationInstance.class, maxillaryId).getScope();
        Scope toothScope = em.find(PrestationInstance.class, toothId).getScope();

        assertThat(maxillaryScope.getMaxillary()).isEqualTo(Maxillary.UPPER);
        assertThat(maxillaryScope.getTooth()).isEmpty();
        assertThat(toothScope.getTooth()).containsExactly(Tooth.T24);
        assertThat(toothScope.getToothFace()).isEqualTo(ToothFace.TOP);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM prestations_instances_tooth WHERE prestation_instance_id = ?", Integer.class, toothId))
                .isEqualTo(1);
    }

    /**
     * CASO: Prestación que referencia un TreatmentInstance y no trae scope propio (build con ownScope = null), flush + clear.
     * Regla: decisión 2 y § 4 — build guarda Scope.empty() y full_mouth es NOT NULL.
     * Validación: el flush no viola el NOT NULL; al recargar scope no es null y está vacío (sin dientes, sin nivel, fullMouth false).
     */
    @Test
    void prestationInstance_persistWithoutOwnScope_reloadReturnsEmptyScope() {
        TreatmentInstance treatmentInstance = persistTreatmentInstance(bety, toothScope(Tooth.T16), TreatmentConditionType.IN_PROGRESS);
        Long id = persistPrestationInstance(bety, treatmentInstance, null, PrestationInstanceStatus.IN_PROGRESS).getId();
        flushAndClear();

        Scope reloaded = em.find(PrestationInstance.class, id).getScope();

        assertThat(reloaded).isNotNull();
        assertThat(reloaded.getTooth()).isEmpty();
        assertThat(reloaded.getToothFace()).isNull();
        assertThat(reloaded.getQuadrant()).isNull();
        assertThat(reloaded.getMaxillary()).isNull();
        assertThat(reloaded.isFullMouth()).isFalse();
        assertThat(reloaded.isEmpty()).isTrue();
    }

    // ─── Queries por paciente ─────────────────────────────────────────────────

    /**
     * CASO: Bety (insertada por el test) con tratamientos REQUIRED, IN_PROGRESS, PREEXISTING, FINISHED, REPLACED y CANCELED,
     * y un REQUIRED de Alan (también insertado por el test).
     * Regla: D-35 — activos son solo REQUIRED e IN_PROGRESS, y solo del paciente consultado. CANCELED (D-26) no es activo:
     * una query escrita con negación (NOT IN) lo dejaría pasar.
     * Validación: devuelve exactamente los ids de los 2 tratamientos REQUIRED e IN_PROGRESS de Bety, con su lista de
     * dientes cargada (no prueba @BatchSize ni el modo de fetch).
     */
    @Test
    void findActiveByPatientId_returnsOnlyRequiredAndInProgressOfPatient() {
        Long requiredId = persistTreatmentInstance(bety, toothScope(Tooth.T11), TreatmentConditionType.REQUIRED).getId();
        Long inProgressId = persistTreatmentInstance(bety, toothScope(Tooth.T12), TreatmentConditionType.IN_PROGRESS).getId();
        persistTreatmentInstance(bety, toothScope(Tooth.T13), TreatmentConditionType.PREEXISTING);
        persistTreatmentInstance(bety, toothScope(Tooth.T14), TreatmentConditionType.FINISHED);
        persistTreatmentInstance(bety, toothScope(Tooth.T15), TreatmentConditionType.REPLACED);
        persistTreatmentInstance(bety, toothScope(Tooth.T16), TreatmentConditionType.CANCELED);
        persistTreatmentInstance(alan, toothScope(Tooth.T16), TreatmentConditionType.REQUIRED);
        flushAndClear();

        List<TreatmentInstance> active = treatmentInstanceRepository.findActiveByPatientId(bety.getId());

        assertThat(active).extracting(TreatmentInstance::getId).containsExactlyInAnyOrder(requiredId, inProgressId);
        assertThat(active).extracting(TreatmentInstance::getTreatmentCondition)
                .containsExactlyInAnyOrder(TreatmentConditionType.REQUIRED, TreatmentConditionType.IN_PROGRESS);
        assertThat(active).allSatisfy(t -> assertThat(t.getScope().getTooth()).hasSize(1));
        assertThat(active).allSatisfy(t -> assertThat(t.getPatient().getId()).isEqualTo(bety.getId()));
    }

    /**
     * CASO: Prestaciones de Bety (insertada por el test) IN_PROGRESS, FINISHED, REPLACED y CANCELED, y una IN_PROGRESS de
     * Alan (también insertado por el test).
     * Regla: D-37 — solo las IN_PROGRESS del paciente entran a la comparación de solapamiento. CANCELED (D-26) no entra.
     * Validación: devuelve exactamente la IN_PROGRESS de Bety, por id.
     */
    @Test
    void findInProgressByPatientId_returnsOnlyInProgressOfPatient() {
        Long inProgressId = persistPrestationInstance(bety, null, toothScope(Tooth.T24), PrestationInstanceStatus.IN_PROGRESS).getId();
        persistPrestationInstance(bety, null, toothScope(Tooth.T25), PrestationInstanceStatus.FINISHED);
        persistPrestationInstance(bety, null, toothScope(Tooth.T26), PrestationInstanceStatus.REPLACED);
        persistPrestationInstance(bety, null, toothScope(Tooth.T28), PrestationInstanceStatus.CANCELED);
        persistPrestationInstance(alan, null, toothScope(Tooth.T27), PrestationInstanceStatus.IN_PROGRESS);
        flushAndClear();

        List<PrestationInstance> inProgress = prestationInstanceRepository.findInProgressByPatientId(bety.getId());

        assertThat(inProgress).extracting(PrestationInstance::getId).containsExactly(inProgressId);
    }

    /**
     * CASO: Tratamiento de Alan consultado con el id de Bety.
     * Regla: D-60 — un id de otro paciente se ve igual que uno inexistente.
     * Validación: Optional.empty() con el paciente equivocado y presente con el correcto.
     */
    @Test
    void treatmentInstanceFindByIdAndPatientId_otherPatient_returnsEmpty() {
        Long id = persistTreatmentInstance(alan, toothScope(Tooth.T16), TreatmentConditionType.REQUIRED).getId();
        flushAndClear();

        assertThat(treatmentInstanceRepository.findByIdAndPatientId(id, bety.getId())).isEmpty();
        assertThat(treatmentInstanceRepository.findByIdAndPatientId(id, alan.getId())).isPresent();
    }

    /**
     * CASO: Prestación de Alan (la 812 del ejemplo) consultada con el id de Bety.
     * Regla: D-60 — mismo criterio que el tratamiento.
     * Validación: Optional.empty() con el paciente equivocado y presente con el correcto.
     */
    @Test
    void prestationInstanceFindByIdAndPatientId_otherPatient_returnsEmpty() {
        Long id = persistPrestationInstance(alan, null, toothScope(Tooth.T24), PrestationInstanceStatus.IN_PROGRESS).getId();
        flushAndClear();

        assertThat(prestationInstanceRepository.findByIdAndPatientId(id, bety.getId())).isEmpty();
        assertThat(prestationInstanceRepository.findByIdAndPatientId(id, alan.getId())).isPresent();
    }
}
