package com.odontologiaintegralfm.dto;

/**
 * @author [Facundo Palmieri]
 */
public record ScheduleResponseDTO(
        Long id,
        String label,
        String cron
) {
}
