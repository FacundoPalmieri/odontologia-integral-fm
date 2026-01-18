package com.odontologiaintegralfm.feature.appointment.core.dto;

import com.odontologiaintegralfm.feature.appointment.core.enums.SlotStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalTime;

/**
 * DTO que representa los slot y su estado.
 * Se utiliza embebido en {@link CalendarDayResponseDTO} para respuesta diaria.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SlotResponseDTO {
    private LocalTime startTime;
    private LocalTime endTime;
    private SlotStatus status;
    private String color;
    private AppointmentResponseDTO appointment;
    private DentistCalendarLockResponseDTO calendarLock;
}
