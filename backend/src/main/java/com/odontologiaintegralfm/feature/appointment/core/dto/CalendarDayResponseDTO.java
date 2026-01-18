package com.odontologiaintegralfm.feature.appointment.core.dto;


import java.time.LocalDate;
import java.util.List;

/**
 * DTO para respuesta de calendario vista diaria
 */
public record CalendarDayResponseDTO(
        Long dentistId,
        LocalDate day,
        CalendarDayStatusResponseDTO calendarDayStatus,//Se devuelve para luego sacar el estado del mes.
        CalendarHolidayResponseDTO holiday,
        List<SlotResponseDTO> slots
) {
}
