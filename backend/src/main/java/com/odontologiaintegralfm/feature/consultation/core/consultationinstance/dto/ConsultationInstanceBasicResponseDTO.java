package com.odontologiaintegralfm.feature.consultation.core.consultationinstance.dto;

import com.odontologiaintegralfm.feature.consultation.core.consultation.dto.ConsultationResponseDTO;

public record ConsultationInstanceBasicResponseDTO(
        Long id,
        ConsultationResponseDTO consultationResponseDTO
) {}