package com.odontologiaintegralfm.feature.appointment.dto;

import java.time.YearMonth;

/**
 * DTO para solicitud de calendario vista Mensual.
 */
public record CalendarMonthRequestDTO(
        Long id,
        YearMonth month
) {
}
