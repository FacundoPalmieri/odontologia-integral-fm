package com.odontologiaintegralfm.feature.consultation.core.prestation.dto;

import com.odontologiaintegralfm.feature.consultation.core.odontogram.dto.OdontogramResponseDTO;

import java.math.BigDecimal;

public record PrestationInstanceResponseDTO(
        Long id,
        String name,

        OdontogramResponseDTO odontogram,

        String scope,

        /**  tooth, tooth face ,quadrant,maxillary  */
        String scopeDetail,

        String status,

        BigDecimal price,

        String promotion,
        BigDecimal promotionAmount,

        String discountType,
        BigDecimal discountValue,
        BigDecimal discountAmount,

        BigDecimal finalAmount
) {
}
