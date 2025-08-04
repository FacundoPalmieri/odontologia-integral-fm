package com.odontologiaintegralfm.dto.internal;

/**
 * @author [Facundo Palmieri]
 */
public record SchedulerResultDTO(
        double durationSeconds,
        String message,
        int countInit,
        int countDeleted
) {
}
