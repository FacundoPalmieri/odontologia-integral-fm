package com.odontologiaintegralfm.feature.appointment.core.dto;

import com.odontologiaintegralfm.feature.appointment.core.enums.CalendarDayStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO para respuesta de calendario vista diaria
 */
public record CalendarDetailDayResponseDTO(
        Long dentistId,
        LocalDate day,
        CalendarDayStatus calendarDayStatus,//Se devuelve para luego sacar el estado del mes.
        List<SlotResponseDTO> slots
) {
}
