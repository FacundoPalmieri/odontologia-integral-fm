package com.odontologiaintegralfm.feature.consultation.core.dto;

import jakarta.validation.constraints.NotEmpty;

/**
 * DTO que se utiliza para la corrección de un odontograma.
 */
public record OdontogramCorrectionRequestDTO(

        OdontogramCreateRequestDTO odontogram,

        @NotEmpty(message = "ConsultationOdontogramCorrectionRequestDTO.observation.empty")
        String observationCorrection
) {
}
