package com.odontologiaintegralfm.feature.consultation.catalogs.promotion.dto;

import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.enums.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PromotionCreateRequestDTO(

        @NotBlank(message = "promotionCreateRequestDTO.label.empty")
        String label,

        @NotNull(message = "promotionCreateRequestDTO.discountType.empty")
        DiscountType discountType,

        @NotNull(message = "promotionCreateRequestDTO.value.empty")
        BigDecimal value,

        @NotNull(message = "promotionCreateRequestDTO.startDate.empty")
        LocalDate startDate,

        @NotNull(message = "promotionCreateRequestDTO.endDate.empty")
        LocalDate endDate
) {
}