package com.odontologiaintegralfm.feature.appointment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * DTO que se utiliza para la creación de un nuevo feriado
 */
public record HolidayCreateRequestDTO(
        @NotNull(message = "HolidayCreateRequestDTO.date.empty")
        LocalDate date,

        @NotBlank(message = "HolidayCreateRequestDTO.type.empty")
        String type,

        @NotBlank(message = "HolidayCreateRequestDTO.name.empty")
        String name
) {
}
