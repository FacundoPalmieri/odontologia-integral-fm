package com.odontologiaintegralfm.feature.consultation.prestationinstance.dto;

import com.odontologiaintegralfm.feature.consultation.prestationinstance.enums.PrestationStepStatus;

/**
 * Un step del catálogo de una prestación. {@code id} es el de la instancia (si ya existe) y {@code stepId} el del
 * catálogo (solo si todavía no hay instancia); {@code status} es nulo si el step está pendiente.
 */
public record StepContextResponseDTO(
        Long id,
        Long stepId,
        String label,
        boolean required,
        PrestationStepStatus status
) {
}