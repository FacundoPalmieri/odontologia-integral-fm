package com.odontologiaintegralfm.feature.appointment.core.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO para respuesta de calendario vista Semanal.
 */
public record CalendarDetailDayResponseDTO(
        Long dentistId,
        LocalDate day,
        List<SlotResponseDTO> slots
) {
}
