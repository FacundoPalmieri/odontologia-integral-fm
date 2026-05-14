package com.odontologiaintegralfm.feature.consultation.core.consultation.dto;


public record ConsultationResponseDTO(
        Long id,
        String patientName,
        String dentistName,
        String consultationStatus
) {}
