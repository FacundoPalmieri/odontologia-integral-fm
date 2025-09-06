package com.odontologiaintegralfm.infrastructure.scheduler.dto.internal;

/**
 * DTO interno del backend. No tiene interacción con el cliente.
 */
public record SchedulerResultDTO(
        double durationSeconds,
        String message,
        int countInit,
        int countDeleted
) {
}
