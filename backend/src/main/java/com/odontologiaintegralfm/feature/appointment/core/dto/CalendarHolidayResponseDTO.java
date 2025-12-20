package com.odontologiaintegralfm.feature.appointment.core.dto;

import com.odontologiaintegralfm.feature.appointment.core.enums.CalendarHoliday;

/**
 * DTO que se devuelve dentro de la respuesta en las vistas de calendario.
 */
public record CalendarHolidayResponseDTO(
        String key,
        String label,
        String description,
        String type,
        String color
) {

    public static CalendarHolidayResponseDTO build (CalendarHoliday calendarHoliday, String description, String type) {
        return new CalendarHolidayResponseDTO(
                calendarHoliday.name(),
                calendarHoliday.getLabel(),
                description,
                type,
                calendarHoliday.getColorHex()
        );
    }
}
