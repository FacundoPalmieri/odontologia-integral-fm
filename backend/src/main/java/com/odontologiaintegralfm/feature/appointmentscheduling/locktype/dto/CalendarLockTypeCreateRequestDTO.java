package com.odontologiaintegralfm.feature.appointmentscheduling.locktype.dto;


import com.odontologiaintegralfm.feature.appointmentscheduling.locktype.enums.CalendarLockMode;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record CalendarLockTypeCreateRequestDTO(
        @NotNull(message = "calendarLockTypeCreateRequestDTO.name.empty")
        String name,

        @NotNull(message = "dentistCalendarLockRequestCreateDTO.mode.empty")
        Set<CalendarLockMode> mode
) {
}
