package com.odontologiaintegralfm.feature.consultation.core.consultation.dto;

import com.odontologiaintegralfm.feature.consultation.core.consultation.model.Consultation;

import java.time.LocalDateTime;

public record ConsultationHistoryResponseDTO(
        Long consultationId,
        Long consultationInstanceId,
        LocalDateTime dateTime,
        String dentistName,
        String consultationStatus
) {

    public static ConsultationHistoryResponseDTO build(Consultation consultation, Long consultationInstanceId) {
        return new ConsultationHistoryResponseDTO(
                consultation.getId(),
                consultationInstanceId,
                consultation.getAppointment().getDate(),
                consultation.getDentist().getPerson().getFullName(),
                consultation.getStatus().name()
        );
    }
}