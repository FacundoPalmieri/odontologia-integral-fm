package com.odontologiaintegralfm.feature.consultation.prestation.repository;

import com.odontologiaintegralfm.feature.consultation.prestation.model.PrestationTypePrice;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
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
        String testPrestations = "(SELECT id FROM prestations_types WHERE name LIKE '%-Test')";
        jdbcTemplate.update("DELETE FROM prestations_types_prices WHERE prestation_type_id IN " + testPrestations);
        jdbcTemplate.update("DELETE FROM prestation_type_scopes WHERE prestation_type_id IN " + testPrestations);
        jdbcTemplate.update("DELETE FROM prestations_types WHERE name LIKE '%-Test'");
    }

    // ─── Helpers (REQ-039) ────────────────────────────────────────────────────

    private Long insertPrestationType(String name, boolean enabled) {
        jdbcTemplate.update("""
                INSERT INTO prestations_types (name, label, is_unique, requires_origin_treatment, can_be_origin, enabled, created_at, created_by_id)
                VALUES (?, ?, false, false, false, ?, NOW(), ?)
                """, name, name, enabled, AUDIT_USER_ID);
        return jdbcTemplate.queryForObject("SELECT id FROM prestations_types WHERE name = ?", Long.class, name);
    }

    private void insertPrice(Long prestationTypeId, String price, LocalDate startDate, LocalDate endDate, boolean enabled) {
        jdbcTemplate.update("""
                INSERT INTO prestations_types_prices (prestation_type_id, price, start_date, end_date, enabled, created_at, created_by_id)
                VALUES (?, ?, ?, ?, ?, NOW(), ?)
                """, prestationTypeId, new BigDecimal(price), startDate, endDate, enabled, AUDIT_USER_ID);
    }

    private static List<Long> prestationIds(List<PrestationTypePrice> prices) {
        return prices.stream().map(price -> price.getPrestationType().getId()).toList();
    }

    /**
     * CASO: Una PrestationType con enabled=false tiene un PrestationTypePrice vigente (startDate=hoy, endDate=null).
     * Regla: Las condiciones explícitas AND pt.enabled = true en la query excluyen la prestación deshabilitada.
     * Validación: findAllActive retorna lista vacía; la prestación deshabilitada no aparece en ningún elemento.
     */
    @Test
    void findAllActive_disabledPrestationType_notReturned() {
        jdbcTemplate.update("""
                INSERT INTO prestations_types (name, label, is_unique, requires_origin_treatment, can_be_origin, enabled, created_at, created_by_id)
                VALUES (?, ?, false, false, false, false, NOW(), ?)
                """, "Prestacion-Deshabilitada-Test", "Prestacion Deshabilitada Test", AUDIT_USER_ID);

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
                INSERT INTO prestations_types (name, label, is_unique, requires_origin_treatment, can_be_origin, enabled, created_at, created_by_id)
                VALUES (?, ?, false, false, false, true, NOW(), ?)
                """, "Prestacion-SinPrecioVigente-Test", "Prestacion Sin Precio Vigente Test", AUDIT_USER_ID);

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

    // ─── REQ-039: precios vigentes a una fecha ────────────────────────────────
    // La fecha es fija (no "hoy"): las ventanas de cada precio se declaran respecto de ASOF.

    private static final LocalDate ASOF = LocalDate.of(2026, 11, 9);

    /**
     * CASO: "SinScopes-Test" está habilitada, no tiene ninguna fila en prestation_type_scopes y tiene un precio vigente
     * (start_date anterior a ASOF, end_date nulo), como PROSTHESIS_RELINE_LAB.
     * Regla: D-04 — los precios a una fecha incluyen las prestaciones sin allowedScopes.
     * Validación: findAllPricesAtDate(ASOF) devuelve el precio de esa prestación.
     */
    @Test
    void findAllPricesAtDate_prestationWithoutAllowedScopes_isIncluded() {
        Long prestation = insertPrestationType("SinScopes-Test", true);
        insertPrice(prestation, "1000.00", ASOF.minusDays(10), null, true);

        List<PrestationTypePrice> result = repository.findAllPricesAtDate(ASOF);

        assertThat(prestationIds(result)).contains(prestation);
    }

    /**
     * CASO: "EmpiezaHoy-Test" tiene un precio con start_date = ASOF y end_date nulo; "TerminaManana-Test" tiene uno con
     * start_date anterior y end_date = ASOF más 1 día.
     * Regla: D-04 — la ventana es startDate <= fecha y endDate nulo o > fecha: el inicio se incluye y el fin no.
     * Validación: findAllPricesAtDate(ASOF) devuelve los dos precios.
     */
    @Test
    void findAllPricesAtDate_priceStartingOnDateOrEndingAfterDate_isIncluded() {
        Long starting = insertPrestationType("EmpiezaHoy-Test", true);
        insertPrice(starting, "1000.00", ASOF, null, true);
        Long ending = insertPrestationType("TerminaManana-Test", true);
        insertPrice(ending, "2000.00", ASOF.minusDays(10), ASOF.plusDays(1), true);

        List<PrestationTypePrice> result = repository.findAllPricesAtDate(ASOF);

        assertThat(prestationIds(result)).contains(starting, ending);
    }

    /**
     * CASO: "Vencido-Test" tiene un precio con end_date = ASOF menos 1 día; "Futuro-Test" tiene uno con start_date = ASOF más 1 día.
     * Regla: D-04 — un precio que ya terminó o que todavía no empezó no es vigente a la fecha.
     * Validación: findAllPricesAtDate(ASOF) no devuelve ninguno de los dos.
     */
    @Test
    void findAllPricesAtDate_priceOutsideWindow_notReturned() {
        Long expired = insertPrestationType("Vencido-Test", true);
        insertPrice(expired, "1000.00", ASOF.minusDays(10), ASOF.minusDays(1), true);
        Long future = insertPrestationType("Futuro-Test", true);
        insertPrice(future, "2000.00", ASOF.plusDays(1), null, true);

        List<PrestationTypePrice> result = repository.findAllPricesAtDate(ASOF);

        assertThat(prestationIds(result)).doesNotContain(expired, future);
    }

    /**
     * CASO: "CambioDePrecio-Test" tiene dos precios: el viejo con start_date anterior y end_date = ASOF, y el nuevo con
     * start_date = ASOF y end_date nulo.
     * Regla: D-04 y D-11 — end_date es el día en que empieza el precio siguiente y no se incluye: el día del cambio rige
     * solo el nuevo.
     * Validación: findAllPricesAtDate(ASOF) devuelve un solo precio de esa prestación, y es el nuevo (1200.00).
     */
    @Test
    void findAllPricesAtDate_priceEndingOnDate_notReturned() {
        Long prestation = insertPrestationType("CambioDePrecio-Test", true);
        insertPrice(prestation, "1000.00", ASOF.minusDays(10), ASOF, true);
        insertPrice(prestation, "1200.00", ASOF, null, true);

        List<PrestationTypePrice> result = repository.findAllPricesAtDate(ASOF);

        assertThat(result)
                .filteredOn(price -> price.getPrestationType().getId().equals(prestation))
                .extracting(PrestationTypePrice::getPrice)
                .containsExactly(new BigDecimal("1200.00"));
    }

    /**
     * CASO: "PrecioBaja-Test" está habilitada pero su precio vigente está dado de baja (enabled = false); "PrestacionBaja-Test"
     * está dada de baja y tiene un precio vigente habilitado.
     * Regla: D-04 — solo cuentan los precios habilitados de prestaciones habilitadas.
     * Validación: findAllPricesAtDate(ASOF) no devuelve ninguna de las dos prestaciones.
     */
    @Test
    void findAllPricesAtDate_disabledPriceOrDisabledType_notReturned() {
        Long disabledPrice = insertPrestationType("PrecioBaja-Test", true);
        insertPrice(disabledPrice, "1000.00", ASOF.minusDays(10), null, false);
        Long disabledType = insertPrestationType("PrestacionBaja-Test", false);
        insertPrice(disabledType, "2000.00", ASOF.minusDays(10), null, true);

        List<PrestationTypePrice> result = repository.findAllPricesAtDate(ASOF);

        assertThat(prestationIds(result)).doesNotContain(disabledPrice, disabledType);
    }

    /**
     * CASO: "DosPrecios-Test" tiene dos precios que se superponen en ASOF: uno desde 10 días antes hasta 5 días después y
     * otro desde 3 días antes sin fecha de fin.
     * Regla: D-04 y D-11 — la consulta devuelve los dos para que el use case pueda detectar el error de datos y responder 409.
     * Validación: findAllPricesAtDate(ASOF) devuelve dos precios con el id de esa prestación.
     */
    @Test
    void findAllPricesAtDate_twoPricesForSamePrestation_returnsBoth() {
        Long prestation = insertPrestationType("DosPrecios-Test", true);
        insertPrice(prestation, "1000.00", ASOF.minusDays(10), ASOF.plusDays(5), true);
        insertPrice(prestation, "1200.00", ASOF.minusDays(3), null, true);

        List<PrestationTypePrice> result = repository.findAllPricesAtDate(ASOF);

        assertThat(Collections.frequency(prestationIds(result), prestation)).isEqualTo(2);
    }
}
