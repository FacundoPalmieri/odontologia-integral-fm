package com.odontologiaintegralfm.feature.appointment.dto;


import java.time.LocalDate;


public record HolidayResponseDTO(
        Long id,
        LocalDate date,
        String type,
        String name
) {
}
