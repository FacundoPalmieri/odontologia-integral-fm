package com.odontologiaintegralfm.feature.appointment.core.dto;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record DentistCalendarLockCreateRequestDTO(

        @NotNull(message = "dentistCalendarLockCreateRequestDTO.lockType.empty")
        Long idLockType,

        @NotEmpty(message = "dentistCalendarLockCreateRequestDTO.recurrence.empty")
        String recurrence,

        @NotNull(message = "dentistCalendarLockCreateRequestDTO.startDate.empty")
        LocalDate startDate,

        @NotNull(message = "dentistCalendarLockCreateRequestDTO.endDate.empty")
        LocalDate endDate,

        @NotNull(message = "dentistCalendarLockCreateRequestDTO.startTime.empty")
        LocalTime startTime,

        @NotNull(message = "dentistCalendarLockCreateRequestDTO.endTime.empty")
        LocalTime endTime,

        String observation

) {
}
