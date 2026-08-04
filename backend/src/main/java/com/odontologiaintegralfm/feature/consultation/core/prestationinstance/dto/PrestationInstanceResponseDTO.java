package com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto;

import com.odontologiaintegralfm.feature.consultation.core.odontogram.dto.OdontogramResponseDTO;

import java.math.BigDecimal;
import java.util.List;

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

        BigDecimal finalAmount,

        BigDecimal pendingAmount,

        // steps vigentes (enabled=true) de esta prestación — para que el front arme stepAdvancements en la próxima corrección
        List<PrestationStepInstanceResponseDTO> steps
) {
}
