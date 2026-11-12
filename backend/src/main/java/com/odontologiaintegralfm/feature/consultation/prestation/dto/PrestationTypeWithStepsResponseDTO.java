package com.odontologiaintegralfm.feature.consultation.prestation.dto;

import com.odontologiaintegralfm.feature.consultation.prestation.enums.PrestationScopeType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public record PrestationTypeWithStepsResponseDTO(
        Long id,
        String name,
        String label,
        boolean isUnique,
        Integer maxTooth,
        boolean requiresOriginTreatment,
        Set<PrestationScopeType> allowedScopes,
        BigDecimal price,
        List<PrestationStepResponseDTO> steps
) {
}
