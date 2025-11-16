package com.odontologiaintegralfm.feature.appointment.core.dto;


import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record DentistHolidayRequestCreateDTO(

        @NotNull(message = "dentistHolidayRequestCreateDTO.year.empty")
        Integer year,

        @NotNull(message = "generic.id.empty")
        Long idHoliday,

        @NotNull(message = "dentistHolidayDTO.startTime.empty")
        LocalTime startTime,

        @NotNull(message = "dentistHolidayDTO.endTime.empty")
        LocalTime endTime
) {
}
