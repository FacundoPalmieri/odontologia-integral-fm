package com.odontologiaintegralfm.feature.consultation.core.dto;

/**
 * @author [Facundo Palmieri]
 */
public record ConsultationCreateRequestDTO(
        Long idPatient,
        String status
) {
}
