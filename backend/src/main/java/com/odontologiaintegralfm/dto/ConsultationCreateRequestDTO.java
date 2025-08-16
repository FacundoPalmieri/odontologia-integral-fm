package com.odontologiaintegralfm.dto;

/**
 * @author [Facundo Palmieri]
 */
public record ConsultationCreateRequestDTO(
        Long idPatient,
        String status
) {
}
