package com.odontologiaintegralfm.infrastructure.externalapi.dto;


public record HolidayApiResponseDTO(
        String fecha,
        String tipo,
        String nombre
) {
}
