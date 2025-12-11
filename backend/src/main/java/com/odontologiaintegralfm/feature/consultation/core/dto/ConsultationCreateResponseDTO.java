package com.odontologiaintegralfm.feature.consultation.core.dto;


import com.odontologiaintegralfm.feature.consultation.core.enums.ConsultationStatus;

import java.time.LocalDateTime;

public record ConsultationCreateResponseDTO(
        Long id,
        String patientName,
        String dentistName,
        String consultationStatus
)
{}
