package com.odontologiaintegralfm.feature.appointment.core.dto;

import java.time.LocalDate;

/**
 * DTO para solicitud de calendario vista Diaria.
 */
public record CalendarDayRequestDTO(
        Long dentistId,
        LocalDate day
) {
}
