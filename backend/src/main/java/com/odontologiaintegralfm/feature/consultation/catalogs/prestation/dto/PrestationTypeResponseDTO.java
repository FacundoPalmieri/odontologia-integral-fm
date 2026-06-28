package com.odontologiaintegralfm.feature.consultation.catalogs.prestation.dto;

import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.enums.PrestationScopeType;

import java.math.BigDecimal;
import java.util.Set;

public record PrestationTypeResponseDTO(
        Long id,
        String name,
        boolean isUnique,
        boolean hasSteps,
        boolean requiresLocation,
        Set<PrestationScopeType> allowedScopes,
        BigDecimal currentPrice
) {
}
