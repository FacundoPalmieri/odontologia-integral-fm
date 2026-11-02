package com.odontologiaintegralfm.feature.consultation.prestationinstance.dto;

import com.odontologiaintegralfm.feature.consultation.prestationinstance.enums.PrestationStepStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

/**
 * Step de una prestación: con id avanza un step existente; con stepId nace uno nuevo del catálogo (D-22, D-33).
 */
public record StepItemDTO(
        Long id,
        Long stepId,
        @NotNull(message = "stepItemDTO.status.empty")
        PrestationStepStatus status
) {

    @AssertTrue(message = "stepItemDTO.idXorStepId")
    public boolean isIdXorStepId() {
        return (id != null) != (stepId != null);
    }
}
