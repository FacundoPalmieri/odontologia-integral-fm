package com.odontologiaintegralfm.feature.appointmentscheduling.calendar.dto;

import java.util.List;

/**
 * DTO para respuesta de calendario vista Mensual.
 */
public record CalendarMonthResponseDTO(
        Integer year,
        Integer month,
        List<CalendarDayResponseDTO> days
) {
}
