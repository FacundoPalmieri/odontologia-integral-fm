package com.odontologiaintegralfm.feature.appointment.core.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

/**
 * DTO que se usa dentro de la lista en {@link DentistHolidayRequestDTO}
 */
public record DentistHolidayListRequestDTO(

        @NotNull(message = "generic.id.empty")
        Long idHoliday,

        @NotNull(message = "dentistHolidayDTO.startTime.empty")
        LocalTime startTime,

        @NotNull(message = "dentistHolidayDTO.endTime.empty")
        LocalTime endTime
) {
}
