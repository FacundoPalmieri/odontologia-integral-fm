package com.odontologiaintegralfm.feature.consultation.core.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * DTO que se utiliza para la corrección de un odontograma.
 */
public record ConsultationOdontogramCorrectionRequestDTO(

        ConsultationOdontogramCreateRequestDTO odontogram,

        @NotEmpty(message = "ConsultationOdontogramCorrectionRequestDTO.observation.empty")
        String observationCorrection
) {
}
