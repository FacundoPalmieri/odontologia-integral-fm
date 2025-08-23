package com.odontologiaintegralfm.feature.appointment.dto;

import java.time.LocalDate;

/**
 * DTO para solicitud de calendario vista Diaria.
 */
public record CalendarDayRequestDTO(
        Long dentistId,
        LocalDate day
) {
}
