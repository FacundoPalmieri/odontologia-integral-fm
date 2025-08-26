package com.odontologiaintegralfm.infrastructure.externalapi;

/**
 * @author [Facundo Palmieri]
 */
public record HolidayApiResponseDTO(
        String fecha,
        String tipo,
        String nombre
) {
}
