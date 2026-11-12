package com.odontologiaintegralfm.feature.consultation.treatment.repository;

import com.odontologiaintegralfm.feature.consultation.treatment.enums.TreatmentConditionType;
import com.odontologiaintegralfm.feature.consultation.treatment.model.Treatment;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de integración de ITreatmentRepository.findAllBySpecialtyWithConditions (REQ-039, D-02, D-12).
 * Requiere las especialidades del script de altas iniciales (Ortodoncia y Odontopediatria) en la base del perfil test
 * (ddl-auto=validate). Cada test inserta sus tratamientos "-Test" por JDBC y los borra en el @AfterEach; si faltan las
 * especialidades, el test falla con un mensaje claro en vez de saltearse.
 */
@Tag("integration")
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ITreatmentRepositoryTest {

    private static final long AUDIT_USER_ID = 4L;

    @Autowired
    private ITreatmentRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @AfterEach
    void cleanup() {
        String testTreatments = "(SELECT id FROM treatment WHERE name LIKE '%-Test')";
        jdbcTemplate.update("DELETE FROM treatment_conditions WHERE treatment_id IN " + testTreatments);
        jdbcTemplate.update("DELETE FROM treatments_dentist_specialties WHERE treatment_id IN " + testTreatments);
        jdbcTemplate.update("DELETE FROM treatment WHERE name LIKE '%-Test'");
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Long specialtyId(String name) {
        List<Long> ids = jdbcTemplate.queryForList("SELECT id FROM dental_specialties WHERE name = ?", Long.class, name);
        assertThat(ids)
                .as("La base de test necesita la especialidad '" + name + "' del script de altas iniciales")
                .hasSize(1);
        return ids.get(0);
    }

    private Long insertTreatment(String name, boolean enabled) {
        jdbcTemplate.update("""
                INSERT INTO treatment (label, name, enabled, created_at, created_by_id)
                VALUES (?, ?, ?, NOW(), ?)
                """, name, name, enabled, AUDIT_USER_ID);
        return jdbcTemplate.queryForObject("SELECT id FROM treatment WHERE name = ?", Long.class, name);
    }

    private void addSpecialty(Long treatmentId, Long specialtyId) {
        jdbcTemplate.update("INSERT INTO treatments_dentist_specialties (treatment_id, dentist_specialty_id) VALUES (?, ?)",
                treatmentId, specialtyId);
    }

    private void addCondition(Long treatmentId, TreatmentConditionType condition) {
        jdbcTemplate.update("INSERT INTO treatment_conditions (treatment_id, condition_type) VALUES (?, ?)",
                treatmentId, condition.name());
    }

    private List<Treatment> treatmentsWithId(List<Treatment> result, Long id) {
        return result.stream().filter(treatment -> treatment.getId().equals(id)).toList();
    }

    // ─── Tests ────────────────────────────────────────────────────────────────

    /**
     * CASO: "Tratamiento-Test" es de Odontopediatría y tiene dos condiciones (REQUIRED e IN_PROGRESS).
     * Regla: D-02 — el tratamiento sale si la especialidad pedida está entre las suyas, con sus condiciones cargadas.
     * Validación: pidiendo Odontopediatría, la lista contiene exactamente un elemento con ese id; sus conditions tienen
     * 2 elementos y ya estaban cargadas (isLoaded) antes de tocarlas.
     */
    @Test
    void findAllBySpecialtyWithConditions_matchingSpecialty_isReturnedWithConditions() {
        Long pediatric = specialtyId("Odontopediatria");
        Long treatmentId = insertTreatment("Tratamiento-Test", true);
        addSpecialty(treatmentId, pediatric);
        addCondition(treatmentId, TreatmentConditionType.REQUIRED);
        addCondition(treatmentId, TreatmentConditionType.IN_PROGRESS);

        List<Treatment> result = repository.findAllBySpecialtyWithConditions(pediatric);

        List<Treatment> matching = treatmentsWithId(result, treatmentId);
        assertThat(matching).hasSize(1);
        assertThat(entityManager.getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(matching.get(0), "conditions"))
                .as("las conditions tienen que venir cargadas por la consulta").isTrue();
        assertThat(matching.get(0).getConditions())
                .containsExactlyInAnyOrder(TreatmentConditionType.REQUIRED, TreatmentConditionType.IN_PROGRESS);
    }

    /**
     * CASO: "Ortodoncia-Test" es solo de Ortodoncia y "SinEspecialidad-Test" no tiene ninguna especialidad cargada.
     * Regla: D-02 y D-12 — un tratamiento de otra especialidad, o sin especialidades, no sale (no es "para todos").
     * Validación: pidiendo Odontopediatría, ninguno de los dos ids está en la lista.
     */
    @Test
    void findAllBySpecialtyWithConditions_otherSpecialtyOrNoSpecialties_notReturned() {
        Long pediatric = specialtyId("Odontopediatria");
        Long orthodontics = specialtyId("Ortodoncia");
        Long orthodonticsOnly = insertTreatment("Ortodoncia-Test", true);
        addSpecialty(orthodonticsOnly, orthodontics);
        Long withoutSpecialties = insertTreatment("SinEspecialidad-Test", true);

        List<Treatment> result = repository.findAllBySpecialtyWithConditions(pediatric);

        assertThat(result).extracting(Treatment::getId).doesNotContain(orthodonticsOnly, withoutSpecialties);
    }

    /**
     * CASO: "Baja-Test" es de Odontopediatría pero está dado de baja (enabled = false).
     * Regla: el catálogo solo muestra tratamientos habilitados.
     * Validación: pidiendo Odontopediatría, el id no está en la lista.
     */
    @Test
    void findAllBySpecialtyWithConditions_disabledTreatment_notReturned() {
        Long pediatric = specialtyId("Odontopediatria");
        Long disabled = insertTreatment("Baja-Test", false);
        addSpecialty(disabled, pediatric);

        List<Treatment> result = repository.findAllBySpecialtyWithConditions(pediatric);

        assertThat(result).extracting(Treatment::getId).doesNotContain(disabled);
    }

    /**
     * CASO: "SinCondiciones-Test" es de Odontopediatría y no tiene ninguna fila en treatment_conditions.
     * Regla: un tratamiento sin condiciones sale igual.
     * Validación: pidiendo Odontopediatría, el id está en la lista exactamente una vez, con conditions vacío y ya cargado.
     */
    @Test
    void findAllBySpecialtyWithConditions_treatmentWithoutConditions_isReturned() {
        Long pediatric = specialtyId("Odontopediatria");
        Long treatmentId = insertTreatment("SinCondiciones-Test", true);
        addSpecialty(treatmentId, pediatric);

        List<Treatment> result = repository.findAllBySpecialtyWithConditions(pediatric);

        List<Treatment> matching = treatmentsWithId(result, treatmentId);
        assertThat(matching).hasSize(1);
        assertThat(entityManager.getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(matching.get(0), "conditions"))
                .as("las conditions tienen que venir cargadas por la consulta").isTrue();
        assertThat(matching.get(0).getConditions()).isEmpty();
    }
}