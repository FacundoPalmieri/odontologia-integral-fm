package com.odontologiaintegralfm.feature.consultation.catalogs.promotion.dto;

import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.enums.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PromotionResponseDTO(
        Long id,
        String label,
        DiscountType discountType,
        BigDecimal value,
        LocalDate startDate,
        LocalDate endDate,
        LocalDateTime finishedAt
) {
}