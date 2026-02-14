package com.odontologiaintegralfm.feature.appointmentscheduling.holiday.dto;


import java.time.LocalDate;


public record HolidayResponseDTO(
        Long id,
        LocalDate date,
        String type,
        String name
) {
}
