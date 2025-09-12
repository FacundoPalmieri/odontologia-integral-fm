package com.odontologiaintegralfm.feature.appointment.catalogs.dto;


import java.time.LocalDate;


public record HolidayResponseDTO(
        Long id,
        LocalDate date,
        String type,
        String name
) {
}
