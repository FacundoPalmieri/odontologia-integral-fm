package com.odontologiaintegralfm.feature.appointment.dto;

import java.time.YearMonth;
import java.util.List;

/**
 * DTO para respuesta de calendario vista Mensual.
 */
public record CalendarMonthResponseDTO(
    YearMonth month,
    List<CalendarGlobalDayDTO> days
) {
}
