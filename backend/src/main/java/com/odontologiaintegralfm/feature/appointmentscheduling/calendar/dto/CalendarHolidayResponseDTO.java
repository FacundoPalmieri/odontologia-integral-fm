package com.odontologiaintegralfm.feature.appointmentscheduling.calendar.dto;

import com.odontologiaintegralfm.feature.appointmentscheduling.calendar.enums.CalendarHoliday;

/**
 * DTO que se devuelve dentro de la respuesta en las vistas de calendario.
 */
public record CalendarHolidayResponseDTO(
        Long id,
        String key,
        String label,
        String description,
        String type,
        String color
) {

    public static CalendarHolidayResponseDTO build (Long id, CalendarHoliday calendarHoliday, String description, String type) {
        return new CalendarHolidayResponseDTO(
                id,
                calendarHoliday.name(),
                calendarHoliday.getLabel(),
                description,
                type,
                calendarHoliday.getColorHex()
        );
    }
}
