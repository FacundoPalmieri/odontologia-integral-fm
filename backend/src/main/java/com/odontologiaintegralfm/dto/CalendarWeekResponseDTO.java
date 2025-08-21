package com.odontologiaintegralfm.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO para respuesta de calendario vista Semanal.
 */
public record CalendarWeekResponseDTO(
        LocalDate weekStart,
        LocalDate weekEnd,
        List<CalendarDetailDayResponseDTO> days
) {
}
