package com.odontologiaintegralfm.feature.consultation.prestation.repository;

import com.odontologiaintegralfm.feature.consultation.prestation.model.PrestationStep;
import com.odontologiaintegralfm.feature.consultation.prestation.model.PrestationType;
import com.odontologiaintegralfm.feature.consultation.prestation.model.Step;
import com.odontologiaintegralfm.feature.user.model.UserSec;
import jakarta.persistence.PersistenceUnitUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de la query del catálogo de steps por varios tipos (REQ-027, T-04).
 * Cada test guarda sus datos con el TestEntityManager (la transacción de @DataJpaTest se revierte sola) y hace
 * flush() y clear() antes de llamar a la query, para que la lectura salga de la base y no del contexto de persistencia.
 */
@Tag("integration")
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestJpaAuditingConfig.class)
class IPrestationStepRepositoryTest {

    private static final long AUDIT_USER_ID = 4L;

    @Autowired
    private IPrestationStepRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private int sequence = 0;

    @BeforeEach
    void checkStepLabelColumn() {
        // Falla en vez de saltear: un "skipped" en fase roja se lee como "no falla".
        Integer columns = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND LOWER(table_name) IN ('steps', 'steps_aud')
                  AND column_name = 'label'
                """, Integer.class);
        assertThat(columns)
                .as("La base de test necesita la columna label en steps y en steps_AUD (esquema v30)")
                .isEqualTo(2);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private UserSec auditUser() {
        return entityManager.getEntityManager().getReference(UserSec.class, AUDIT_USER_ID);
    }

    private PrestationType persistType(String name) {
        PrestationType type = new PrestationType();
        type.setName(name + "-Test");
        type.setLabel(name);
        type.setCreatedBy(auditUser());
        return entityManager.persist(type);
    }

    private PrestationStep persistCatalogStep(PrestationType type, int position, String label, boolean enabled) {
        Step step = new Step();
        step.setName("STEP_" + (++sequence) + "_TEST");
        step.setLabel(label);
        step.setCreatedBy(auditUser());
        entityManager.persist(step);

        PrestationStep catalogStep = new PrestationStep();
        catalogStep.setPrestation(type);
        catalogStep.setStep(step);
        catalogStep.setPosition(position);
        catalogStep.setRequired(true);
        catalogStep.setCreatedBy(auditUser());
        if (!enabled) {
            catalogStep.disable(auditUser());
        }
        return entityManager.persist(catalogStep);
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    // ─── Tests ────────────────────────────────────────────────────────────────

    /**
     * CASO: tres tipos de prestación (A, B y C), cada uno con dos steps en el catálogo.
     * Regla: T-04 — se lee el catálogo de varios tipos en una sola consulta, y solo de los pedidos.
     * Validación: findByPrestationIdInFetchStep(Set.of(A, B)) devuelve exactamente los 4 steps de A y B, y ninguno de C.
     */
    @Test
    void findByPrestationIdInFetchStep_twoTypes_returnsOnlyRequestedTypes() {
        PrestationType typeA = persistType("TipoA");
        PrestationType typeB = persistType("TipoB");
        PrestationType typeC = persistType("TipoC");
        PrestationStep a1 = persistCatalogStep(typeA, 1, "A1", true);
        PrestationStep a2 = persistCatalogStep(typeA, 2, "A2", true);
        PrestationStep b1 = persistCatalogStep(typeB, 1, "B1", true);
        PrestationStep b2 = persistCatalogStep(typeB, 2, "B2", true);
        PrestationStep c1 = persistCatalogStep(typeC, 1, "C1", true);
        PrestationStep c2 = persistCatalogStep(typeC, 2, "C2", true);
        flushAndClear();

        List<PrestationStep> result = repository.findByPrestationIdInFetchStep(Set.of(typeA.getId(), typeB.getId()));

        assertThat(result).extracting(PrestationStep::getId)
                .containsExactlyInAnyOrder(a1.getId(), a2.getId(), b1.getId(), b2.getId())
                .doesNotContain(c1.getId(), c2.getId());
    }

    /**
     * CASO: un tipo con dos steps en el catálogo, uno de ellos con enabled = false.
     * Regla: ADR-0008 — lo dado de baja no vuelve (@Where de PrestationStep).
     * Validación: el resultado trae solo el step habilitado.
     */
    @Test
    void findByPrestationIdInFetchStep_disabledStep_notReturned() {
        PrestationType type = persistType("TipoBaja");
        PrestationStep enabledStep = persistCatalogStep(type, 1, "Habilitado", true);
        PrestationStep disabledStep = persistCatalogStep(type, 2, "DadoDeBaja", false);
        flushAndClear();

        List<PrestationStep> result = repository.findByPrestationIdInFetchStep(Set.of(type.getId()));

        assertThat(result).extracting(PrestationStep::getId)
                .containsExactly(enabledStep.getId())
                .doesNotContain(disabledStep.getId());
    }

    /**
     * CASO: un tipo con dos steps en el catálogo, cada uno con su Step.
     * Regla: T-04 — el Step y el tipo vuelven cargados (JOIN FETCH), así que leer Step.getLabel() no hace otra consulta.
     * Validación: para cada PrestationStep devuelto, isLoaded(step, "step") e isLoaded(step, "prestation") son true, y
     * getStep().getLabel() tiene el valor guardado.
     */
    @Test
    void findByPrestationIdInFetchStep_result_hasStepAndTypeAlreadyLoaded() {
        PrestationType type = persistType("TipoFetch");
        persistCatalogStep(type, 1, "Apertura", true);
        persistCatalogStep(type, 2, "Desobturacion", true);
        flushAndClear();

        List<PrestationStep> result = repository.findByPrestationIdInFetchStep(Set.of(type.getId()));

        PersistenceUnitUtil unitUtil = entityManager.getEntityManager().getEntityManagerFactory().getPersistenceUnitUtil();
        assertThat(result).hasSize(2);
        result.forEach(catalogStep -> {
            assertThat(unitUtil.isLoaded(catalogStep, "step")).isTrue();
            assertThat(unitUtil.isLoaded(catalogStep, "prestation")).isTrue();
        });
        assertThat(result).extracting(catalogStep -> catalogStep.getStep().getLabel())
                .containsExactlyInAnyOrder("Apertura", "Desobturacion");
    }
}
