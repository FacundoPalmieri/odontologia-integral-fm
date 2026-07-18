package com.odontologiaintegralfm.feature.consultation.catalogs.promotion.service;

import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.enums.DiscountType;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.enums.PromotionStatus;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.BadRequestException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Reglas de dominio de Promotion compartidas por Create y Update (ADR-0023):
 * derivación de estado por fechas (ADR-0025) y validación de rango de value.
 */
@Service
public class PromotionDomainService {

    /**
     * Deriva el estado de una promoción a partir de finishedAt (corte manual, prioridad absoluta,
     * ADR-0022) o, si es null, de sus fechas persistidas (ADR-0025).
     * Regla: startDate <= hoy <= endDate → ACTIVE (boundaries inclusive).
     */
    public PromotionStatus resolveStatus(LocalDate startDate, LocalDate endDate, LocalDateTime finishedAt) {
        if (finishedAt != null) {
            return PromotionStatus.FINISHED;
        }

        LocalDate today = LocalDate.now();

        if (today.isBefore(startDate)) {
            return PromotionStatus.NOT_STARTED;
        }
        if (today.isAfter(endDate)) {
            return PromotionStatus.FINISHED;
        }
        return PromotionStatus.ACTIVE;
    }

    /**
     * Valida el rango de value según discountType: PERCENTAGE en [1,100], FIXED >= 1.
     * Regla: req § 6.
     */
    public void validateValueRange(DiscountType discountType, BigDecimal value) {
        if (discountType == DiscountType.PERCENTAGE) {
            if (value.compareTo(BigDecimal.ONE) < 0 || value.compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new BadRequestException(
                        "exception.promotionDomainService.percentageOutOfRange.user", null,
                        "exception.promotionDomainService.percentageOutOfRange.log", new Object[]{value, "PromotionDomainService", "validateValueRange"},
                        LogLevel.ERROR);
            }
        } else {
            if (value.compareTo(BigDecimal.ONE) < 0) {
                throw new BadRequestException(
                        "exception.promotionDomainService.fixedBelowMinimum.user", null,
                        "exception.promotionDomainService.fixedBelowMinimum.log", new Object[]{value, "PromotionDomainService", "validateValueRange"},
                        LogLevel.ERROR);
            }
        }
    }
}