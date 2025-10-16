package com.odontologiaintegralfm.feature.appointment.core.dto;


import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record DentistHolidayRequestUpdateDTO(

        @NotNull(message = "generic.id.empty")
        Long idDentistHoliday,

        @NotNull(message = "dentistHolidayDTO.startTime.empty")
        LocalTime startTime,

        @NotNull(message = "dentistHolidayDTO.endTime.empty")
        LocalTime endTime,

        Boolean enabled

) {
}
