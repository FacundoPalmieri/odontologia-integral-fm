package com.odontologiaintegralfm.feature.consultation.core.prestation.dto;

import com.odontologiaintegralfm.feature.consultation.core.prestation.enums.PrestationStepStatus;

public record PrestationStepAdvancementRequestDTO(
        // La prestación existente que se avanza
        Long prestationInstanceId,

        // El step nuevo del catálogo
        Long prestationStepId,

        // IN_PROGRESS o COMPLETED
        PrestationStepStatus status
) {
}
