package com.odontologiaintegralfm.feature.consultation.prestation.repository;

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

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        jdbcTemplate.update("DELETE FROM prestacion_treatment WHERE prestacion_id IN " +
                "(SELECT id FROM prestations_types WHERE name LIKE '%-Test')");
        jdbcTemplate.update("DELETE FROM prestations_types WHERE name LIKE '%-Test'");
        jdbcTemplate.update("DELETE FROM treatment WHERE name LIKE '%-Test'");
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Long insertTreatment(String name) {
        jdbcTemplate.update("""
                INSERT INTO treatment (label, name, enabled, created_at, created_by_id)
                VALUES (?, ?, true, NOW(), ?)
                """, name, name, AUDIT_USER_ID);
        return jdbcTemplate.queryForObject("SELECT id FROM treatment WHERE name = ?", Long.class, name);
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
}