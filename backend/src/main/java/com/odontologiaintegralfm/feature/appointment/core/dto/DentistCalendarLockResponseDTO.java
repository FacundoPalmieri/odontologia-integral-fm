package com.odontologiaintegralfm.feature.appointment.core.dto;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record DentistCalendarLockResponseDTO(
        Long id,
        Long idDentist,
        String lockType,
        String recurrence,
        LocalDate startDate,
        LocalDate endDate,
        LocalTime startTime,
        LocalTime endTime,
        String observation,
        String observationUpdate,
        List<AppointmentConflictResponseDTO> appointmentConflict
) {
}
