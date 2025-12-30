package com.odontologiaintegralfm.feature.appointment.core.dto;

import com.odontologiaintegralfm.feature.appointment.core.enums.CalendarDayStatus;

/**
 * DTO respuesta para devolver el ENUM con su color y descripción.
 */
public record CalendarDayStatusResponseDTO(
        String key,
        String description,
        String color
) {


    public static CalendarDayStatusResponseDTO build(CalendarDayStatus status) {
        return new CalendarDayStatusResponseDTO(
                status.name(),
                status.getDescription(),
                status.getColorHex()
        );
    }
}
