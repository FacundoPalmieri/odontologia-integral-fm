package com.odontologiaintegralfm.feature.consultation.catalogs.promotion.dto;

import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.enums.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PromotionUpdateRequestDTO(

        @NotBlank(message = "promotionUpdateRequestDTO.label.empty")
        String label,

        @NotNull(message = "promotionUpdateRequestDTO.discountType.empty")
        DiscountType discountType,

        @NotNull(message = "promotionUpdateRequestDTO.value.empty")
        BigDecimal value,

        @NotNull(message = "promotionUpdateRequestDTO.startDate.empty")
        LocalDate startDate,

        @NotNull(message = "promotionUpdateRequestDTO.endDate.empty")
        LocalDate endDate
) {
}