package com.odontologiaintegralfm.feature.consultation.catalogs.prestation.repository;

import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.model.PrestationTypePrice;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestJpaAuditingConfig.class)
class IPrestationTypePriceRepositoryTest {

    @Autowired
    private IPrestationTypePriceRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final long AUDIT_USER_ID = 4L;

    @AfterEach
    void cleanup() {
        jdbcTemplate.update(
                "DELETE FROM prestations_types_prices WHERE prestation_type_id IN " +
                "(SELECT id FROM prestations_types WHERE name LIKE '%-Test')");
        jdbcTemplate.update("DELETE FROM prestations_types WHERE name LIKE '%-Test'");
    }

    /**
     * CASO: Una PrestationType con enabled=false tiene un PrestationTypePrice vigente (startDate=hoy, endDate=null).
     * Regla: Las condiciones explícitas AND pt.enabled = true en la query excluyen la prestación deshabilitada.
     * Validación: findAllActive retorna lista vacía; la prestación deshabilitada no aparece en ningún elemento.
     */
    @Test
    void findAllActive_disabledPrestationType_notReturned() {
        jdbcTemplate.update("""
                INSERT INTO prestations_types (name, is_unique, has_steps, requires_location, enabled, created_at, created_by_id)
                VALUES (?, false, false, false, false, NOW(), ?)
                """, "Prestacion-Deshabilitada-Test", AUDIT_USER_ID);

        Long prestationTypeId = jdbcTemplate.queryForObject(
                "SELECT id FROM prestations_types WHERE name = ?", Long.class, "Prestacion-Deshabilitada-Test");

        jdbcTemplate.update("""
                INSERT INTO prestations_types_prices (prestation_type_id, price, start_date, end_date, enabled, created_at, created_by_id)
                VALUES (?, 1000.00, ?, NULL, true, NOW(), ?)
                """, prestationTypeId, LocalDate.now(), AUDIT_USER_ID);

        List<PrestationTypePrice> result = repository.findAllActive(LocalDate.now());

        assertThat(result)
                .noneMatch(ptp -> ptp.getPrestationType().getId().equals(prestationTypeId));
    }

    /**
     * CASO: Una PrestationType con enabled=true tiene un PrestationTypePrice con endDate != null (precio vencido).
     * Regla: La condición endDate IS NULL filtra precios que ya tienen fecha de cierre.
     * Validación: findAllActive retorna lista vacía; la prestación sin precio vigente no aparece.
     */
    @Test
    void findAllActive_noActivePrice_notReturned() {
        jdbcTemplate.update("""
                INSERT INTO prestations_types (name, is_unique, has_steps, requires_location, enabled, created_at, created_by_id)
                VALUES (?, false, false, false, true, NOW(), ?)
                """, "Prestacion-SinPrecioVigente-Test", AUDIT_USER_ID);

        Long prestationTypeId = jdbcTemplate.queryForObject(
                "SELECT id FROM prestations_types WHERE name = ?", Long.class, "Prestacion-SinPrecioVigente-Test");

        jdbcTemplate.update("""
                INSERT INTO prestations_types_prices (prestation_type_id, price, start_date, end_date, enabled, created_at, created_by_id)
                VALUES (?, 1000.00, ?, ?, true, NOW(), ?)
                """, prestationTypeId, LocalDate.now().minusDays(30), LocalDate.now().minusDays(1), AUDIT_USER_ID);

        List<PrestationTypePrice> result = repository.findAllActive(LocalDate.now());

        assertThat(result)
                .noneMatch(ptp -> ptp.getPrestationType().getId().equals(prestationTypeId));
    }
}
