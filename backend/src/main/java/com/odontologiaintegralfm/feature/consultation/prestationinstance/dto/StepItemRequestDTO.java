package com.odontologiaintegralfm.feature.consultation.prestationinstance.dto;

import com.odontologiaintegralfm.feature.consultation.prestationinstance.enums.PrestationStepStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

/**
 * Step de una prestación: con id avanza un step existente; con stepId nace uno nuevo del catálogo (D-22, D-33).
 */
public record StepItemRequestDTO(
        Long id,
        Long stepId,
        @NotNull(message = "stepItemRequestDTO.status.empty")
        PrestationStepStatus status
) {

    @AssertTrue(message = "stepItemRequestDTO.idXorStepId")
    public boolean isIdXorStepId() {
        return (id != null) != (stepId != null);
    }
}
