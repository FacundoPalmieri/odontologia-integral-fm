package com.odontologiaintegralfm.feature.appointmentscheduling.locktype.dto;

import com.odontologiaintegralfm.feature.appointmentscheduling.locktype.enums.CalendarLockMode;

/**
 * DTO que devuelve los modos de bloqueo que sirve para complementar la creación del tipo de evento.
 */
public record CalendarLockModeResponseDTO(
        String name,
        String label,
        String description
) {

    public static CalendarLockModeResponseDTO build(CalendarLockMode mode) {
        return new CalendarLockModeResponseDTO(mode.name(), mode.getLabel(), mode.getDescription());
    }
}
