package com.odontologiaintegralfm.feature.consultation.core.consultation.dto;


import java.time.LocalDateTime;

public record ConsultationResponseDTO(
        Long id,
        Long appointmentId,
        LocalDateTime dateTime,
        Long patientId,
        String patientName,
        String dentistName,
        String consultationStatus,
        String webSocketStatus
) {}
