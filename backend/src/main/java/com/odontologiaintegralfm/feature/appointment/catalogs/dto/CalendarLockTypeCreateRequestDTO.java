package com.odontologiaintegralfm.feature.appointment.catalogs.dto;


import com.odontologiaintegralfm.feature.appointment.catalogs.enums.CalendarLockMode;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record CalendarLockTypeCreateRequestDTO(
        @NotNull(message = "calendarLockTypeCreateRequestDTO.name.empty")
        String name,

        @NotNull(message = "dentistCalendarLockRequestCreateDTO.mode.empty")
        Set<CalendarLockMode> mode
) {
}
