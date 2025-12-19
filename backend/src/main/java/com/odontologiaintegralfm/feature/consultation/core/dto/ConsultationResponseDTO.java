package com.odontologiaintegralfm.feature.consultation.core.dto;


public record ConsultationResponseDTO(
        Long id,
        String patientName,
        String dentistName,
        String consultationStatus
)
{}
