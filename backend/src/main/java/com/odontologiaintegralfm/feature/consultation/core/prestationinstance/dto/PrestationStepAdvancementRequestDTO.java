package com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto;

import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.enums.PrestationStepStatus;

public record PrestationStepAdvancementRequestDTO(
        // La prestación existente que se avanza
        Long prestationInstanceId,

        // El step nuevo del catálogo
        Long prestationStepId,

        // IN_PROGRESS o COMPLETED
        PrestationStepStatus status
) {
}
