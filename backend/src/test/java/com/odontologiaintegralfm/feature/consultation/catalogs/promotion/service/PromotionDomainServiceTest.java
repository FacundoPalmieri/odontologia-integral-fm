package com.odontologiaintegralfm.feature.consultation.catalogs.promotion.service;

import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.enums.DiscountType;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.enums.PromotionStatus;
import com.odontologiaintegralfm.shared.exception.BadRequestException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("unit")
class PromotionDomainServiceTest {

    private final PromotionDomainService domainService = new PromotionDomainService();

    /**
     * CASO: finishedAt seteado con startDate/endDate que darían ACTIVE si se ignorara finishedAt.
     * Regla: finishedAt tiene prioridad absoluta sobre el cálculo por fechas (ADR-0025).
     * Validación: resolveStatus devuelve FINISHED.
     */
    @Test
    void resolveStatus_whenFinishedAtSet_takesPriorityOverActiveDates() {
        LocalDate startDate = LocalDate.now().minusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(5);
        LocalDateTime finishedAt = LocalDateTime.now().minusHours(1);

        PromotionStatus status = domainService.resolveStatus(startDate, endDate, finishedAt);

        assertThat(status).isEqualTo(PromotionStatus.FINISHED);
    }

    /**
     * CASO: finishedAt seteado con startDate posterior a hoy (daría NOT_STARTED si se ignorara finishedAt).
     * Regla: cubre el corte manual de una promoción finalizada antes de arrancar.
     * Validación: resolveStatus devuelve FINISHED.
     */
    @Test
    void resolveStatus_whenFinishedAtSet_takesPriorityOverNotStartedDates() {
        LocalDate startDate = LocalDate.now().plusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(10);
        LocalDateTime finishedAt = LocalDateTime.now().minusHours(1);

        PromotionStatus status = domainService.resolveStatus(startDate, endDate, finishedAt);

        assertThat(status).isEqualTo(PromotionStatus.FINISHED);
    }

    /**
     * CASO: finishedAt = null, endDate anterior a hoy (vencimiento natural).
     * Adaptación de resolveStatus_whenEndDateBeforeToday_returnsFinished con el nuevo parámetro.
     * Validación: resolveStatus devuelve FINISHED.
     */
    @Test
    void resolveStatus_whenFinishedAtNullAndEndDateBeforeToday_returnsFinished() {
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now().minusDays(1);

        PromotionStatus status = domainService.resolveStatus(startDate, endDate, null);

        assertThat(status).isEqualTo(PromotionStatus.FINISHED);
    }

    /**
     * CASO: finishedAt = null, startDate posterior a hoy.
     * Validación: resolveStatus devuelve NOT_STARTED.
     */
    @Test
    void resolveStatus_whenStartDateAfterToday_returnsNotStarted() {
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(10);

        PromotionStatus status = domainService.resolveStatus(startDate, endDate, null);

        assertThat(status).isEqualTo(PromotionStatus.NOT_STARTED);
    }

    /**
     * CASO: finishedAt = null, startDate == hoy (boundary).
     * Regla: startDate <= hoy, no <.
     * Validación: resolveStatus devuelve ACTIVE.
     */
    @Test
    void resolveStatus_whenStartDateEqualsToday_returnsActive() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(10);

        PromotionStatus status = domainService.resolveStatus(startDate, endDate, null);

        assertThat(status).isEqualTo(PromotionStatus.ACTIVE);
    }

    /**
     * CASO: finishedAt = null, hoy está estrictamente entre startDate y endDate.
     * Validación: resolveStatus devuelve ACTIVE.
     */
    @Test
    void resolveStatus_whenTodayBetweenStartAndEnd_returnsActive() {
        LocalDate startDate = LocalDate.now().minusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(5);

        PromotionStatus status = domainService.resolveStatus(startDate, endDate, null);

        assertThat(status).isEqualTo(PromotionStatus.ACTIVE);
    }

    /**
     * CASO: finishedAt = null, endDate == hoy (boundary).
     * Regla: endDate >= hoy, no > — confirmado con el dev como bug más probable en producción.
     * Validación: resolveStatus devuelve ACTIVE.
     */
    @Test
    void resolveStatus_whenEndDateEqualsToday_returnsActive() {
        LocalDate startDate = LocalDate.now().minusDays(5);
        LocalDate endDate = LocalDate.now();

        PromotionStatus status = domainService.resolveStatus(startDate, endDate, null);

        assertThat(status).isEqualTo(PromotionStatus.ACTIVE);
    }

    /**
     * CASO: discountType=PERCENTAGE con value por debajo del mínimo (0).
     * Validación: Lanza BadRequestException.
     */
    @Test
    void validateValueRange_whenPercentageBelowMinimum_throwsBadRequestException() {
        assertThatThrownBy(() -> domainService.validateValueRange(DiscountType.PERCENTAGE, BigDecimal.ZERO))
                .isInstanceOf(BadRequestException.class);
    }

    /**
     * CASO: discountType=PERCENTAGE con value por encima del máximo (101).
     * Validación: Lanza BadRequestException.
     */
    @Test
    void validateValueRange_whenPercentageAboveMaximum_throwsBadRequestException() {
        assertThatThrownBy(() -> domainService.validateValueRange(DiscountType.PERCENTAGE, BigDecimal.valueOf(101)))
                .isInstanceOf(BadRequestException.class);
    }

    /**
     * CASO: discountType=FIXED con value por debajo del mínimo (0).
     * Validación: Lanza BadRequestException.
     */
    @Test
    void validateValueRange_whenFixedBelowMinimum_throwsBadRequestException() {
        assertThatThrownBy(() -> domainService.validateValueRange(DiscountType.FIXED, BigDecimal.ZERO))
                .isInstanceOf(BadRequestException.class);
    }

    /**
     * CASO: discountType=PERCENTAGE con value dentro de rango (50).
     * Validación: No lanza excepción.
     */
    @Test
    void validateValueRange_withValidPercentage_doesNotThrow() {
        assertThatCode(() -> domainService.validateValueRange(DiscountType.PERCENTAGE, BigDecimal.valueOf(50)))
                .doesNotThrowAnyException();
    }

    /**
     * CASO: discountType=FIXED con value dentro de rango (100).
     * Validación: No lanza excepción.
     */
    @Test
    void validateValueRange_withValidFixed_doesNotThrow() {
        assertThatCode(() -> domainService.validateValueRange(DiscountType.FIXED, BigDecimal.valueOf(100)))
                .doesNotThrowAnyException();
    }
}