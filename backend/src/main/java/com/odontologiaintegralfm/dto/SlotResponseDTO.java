package com.odontologiaintegralfm.dto;

import java.time.LocalTime;

/**
 * DTO que representa los slot y su estado.
 * Se utiliza embebido en {@link CalendarDetailDayResponseDTO} para respuesta diaria.
 */
public record SlotResponseDTO(
        LocalTime starTime,
        LocalTime endTime //,
       // AppointmentResponseDTO appointment,
       // CalendarLockDentistResponseDTO calendarLock
) {
}
