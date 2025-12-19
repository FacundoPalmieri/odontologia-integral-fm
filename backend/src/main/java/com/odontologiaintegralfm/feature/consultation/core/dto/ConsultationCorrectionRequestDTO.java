package com.odontologiaintegralfm.feature.consultation.core.dto;

import com.odontologiaintegralfm.feature.consultation.core.enums.ConsultationEventType;
import jakarta.validation.constraints.NotNull;

/**
 * @author [Facundo Palmieri]
 */
public record ConsultationCorrectionRequestDTO(
        @NotNull(message = "consultationCorrectionRequestDTO.eventType.empty")
        ConsultationEventType eventType,

        @NotNull(message = "consultationCorrectionRequestDTO.observation.empty")
        String observation
) {
}
