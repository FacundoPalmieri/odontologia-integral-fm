package com.odontologiaintegralfm.feature.appointment.dto;

import java.time.LocalDate;

/**
 * DTO para solicitud de calendario vista Semanal.
 */
public record CalendarWeekRequestDTO(
         Long dentistId,
         LocalDate weekStart,
         LocalDate weekEnd
) {
}
