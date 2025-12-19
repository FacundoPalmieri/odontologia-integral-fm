package com.odontologiaintegralfm.feature.consultation.core.dto;

import com.odontologiaintegralfm.feature.consultation.core.enums.ConsultationEventType;
import com.odontologiaintegralfm.feature.consultation.core.enums.ConsultationStatusType;
import jakarta.validation.constraints.NotNull;

/**
 * DTO que permite actualizar el estado de una consulta
 */
public record ConsultationUpdateRequestDTO(

        @NotNull(message = "consultationUpdateRequestDTO.status.empty")
        ConsultationStatusType status,

        ConsultationCorrectionRequestDTO correction

) {
}
