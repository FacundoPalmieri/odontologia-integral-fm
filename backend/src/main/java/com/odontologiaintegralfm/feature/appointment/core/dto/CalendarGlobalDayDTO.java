package com.odontologiaintegralfm.feature.appointment.core.dto;

import java.time.LocalDate;

/**
 * DTO con los detalles para la respuesta de {@link CalendarMonthResponseDTO}
 */
public record CalendarGlobalDayDTO(
        LocalDate date,
        String status,      // Enum CalendarDayStatus
        String description, // Enum CalendarDayStatus
        String color        // Enum CalendarDayStatus
) {
}
