package com.odontologiaintegralfm.feature.appointment.catalogs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * DTO para actualizar un feriado.
 */
public record HolidayUpdateRequestDTO(
        @NotNull(message = "generic.id.empty")
        Long id,

        @NotNull(message = "HolidayCreateRequestDTO.date.empty")
        LocalDate date,

        @NotBlank(message = "HolidayCreateRequestDTO.type.empty")
        String type,

        @NotBlank(message = "HolidayCreateRequestDTO.name.empty")
        String name
) {
}
