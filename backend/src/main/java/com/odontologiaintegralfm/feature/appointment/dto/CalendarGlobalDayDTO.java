package com.odontologiaintegralfm.feature.appointment.dto;

import java.time.LocalDate;

/**
 * DTO con los detalles para la respuesta de {@link CalendarMonthResponseDTO}
 */
public record CalendarGlobalDayDTO(
        LocalDate date,
        String status,      // Enum CalendarMonthStatus
        String description, // Enum CalendarMonthStatus
        String color        // Enum CalendarMonthStatus
        ) {
}
