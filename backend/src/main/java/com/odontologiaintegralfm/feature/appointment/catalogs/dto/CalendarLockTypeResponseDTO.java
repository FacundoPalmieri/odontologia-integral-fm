package com.odontologiaintegralfm.feature.appointment.catalogs.dto;


import com.odontologiaintegralfm.feature.appointment.catalogs.model.CalendarLockType;

public record CalendarLockTypeResponseDTO(
        Long id,
        String name,
        boolean absenceTotal,
        boolean allowTimeRange,
        boolean allowDays,
        boolean enabled
) {

    public static CalendarLockTypeResponseDTO from(CalendarLockType lockType) {
        return new CalendarLockTypeResponseDTO(
                lockType.getId(),
                lockType.getName(),
                lockType.isAbsenceTotal(),
                lockType.isAllowTimeRange(),
                lockType.isAllowDays(),
                lockType.isEnabled()
        );
    }

}
