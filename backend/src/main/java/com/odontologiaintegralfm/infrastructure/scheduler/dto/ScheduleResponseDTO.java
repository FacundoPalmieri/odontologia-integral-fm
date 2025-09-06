package com.odontologiaintegralfm.infrastructure.scheduler.dto;

/**
 * @author [Facundo Palmieri]
 */
public record ScheduleResponseDTO(
        Long id,
        String label,
        String cron
) {
}
