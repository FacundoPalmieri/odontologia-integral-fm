package com.odontologiaintegralfm.feature.appointment.core.dto;

import java.time.YearMonth;
import java.util.List;

/**
 * DTO para respuesta de calendario vista Mensual.
 */
public record CalendarMonthResponseDTO(
        Integer year,
        Integer month,
        List<CalendarGlobalDayDTO> days
) {
}
