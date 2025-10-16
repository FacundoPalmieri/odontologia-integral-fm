package com.odontologiaintegralfm.feature.appointment.core.dto;


import java.time.LocalDate;
import java.time.LocalTime;
public record DentistHolidayResponseDTO(
        Long id,
        Long idDentist,
        Long idHoliday,
        LocalDate date,
        String name,
        LocalTime startTime,
        LocalTime endTime,
        boolean enabled
) {
}
