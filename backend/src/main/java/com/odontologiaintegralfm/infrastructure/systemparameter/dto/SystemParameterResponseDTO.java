package com.odontologiaintegralfm.infrastructure.systemparameter.dto;

/**
 * @author [Facundo Palmieri]
 */
public record SystemParameterResponseDTO(
        Long id,
        String value,
        String description

) {
}
