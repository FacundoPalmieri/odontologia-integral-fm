package com.odontologiaintegralfm.dto;

/**
 * @author [Facundo Palmieri]
 */
public record ScheduleConfigResponseDTO(
        Long id,
        String name,
        String label,
        String cron
) {
}
