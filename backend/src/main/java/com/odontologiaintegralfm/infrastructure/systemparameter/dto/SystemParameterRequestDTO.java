package com.odontologiaintegralfm.infrastructure.systemparameter.dto;

import jakarta.validation.constraints.NotNull;

/**
 * @author [Facundo Palmieri]
 */
public record SystemParameterRequestDTO(
        @NotNull(message = "generic.id.empty")
        Long id,
        String value
) {
}
