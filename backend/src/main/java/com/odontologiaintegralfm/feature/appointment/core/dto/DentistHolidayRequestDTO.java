package com.odontologiaintegralfm.feature.appointment.core.dto;


import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record DentistHolidayRequestDTO(

        @NotNull(message = "generic.id.empty")
        Long idDentist,

        @NotNull(message = "generic.id.empty")
        Long idHoliday,

        @NotNull
        LocalTime startTime,

        @NotNull
        LocalTime endTime
) {
}
