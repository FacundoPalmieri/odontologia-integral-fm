package com.odontologiaintegralfm.feature.appointment.catalogs.dto;


import com.odontologiaintegralfm.feature.appointment.catalogs.enums.CalendarLockMode;
import com.odontologiaintegralfm.feature.appointment.catalogs.model.CalendarLockType;
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
