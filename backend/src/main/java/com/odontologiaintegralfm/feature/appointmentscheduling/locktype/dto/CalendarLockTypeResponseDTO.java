package com.odontologiaintegralfm.feature.appointmentscheduling.locktype.dto;


import com.odontologiaintegralfm.feature.appointmentscheduling.locktype.enums.CalendarLockMode;
import com.odontologiaintegralfm.feature.appointmentscheduling.locktype.model.CalendarLockType;
import java.util.Set;

public record CalendarLockTypeResponseDTO(
        Long id,
        String name,
        Set<CalendarLockMode> modes,
        boolean enabled
) {

    public static CalendarLockTypeResponseDTO from(CalendarLockType lockType) {
        return new CalendarLockTypeResponseDTO(
                lockType.getId(),
                lockType.getName(),
                lockType.getModes(),
                lockType.isEnabled()
        );
    }

}
