package com.odontologiaintegralfm.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * @author [Facundo Palmieri]
 */
public record AttachedFileConfigRequestDTO(
        @NotNull(message = "generic.id.empty")
        Long id,

        @Min(value = 1, message = "attachedFileConfigRequestDTO.days.empty")
        Long days
) {
}
