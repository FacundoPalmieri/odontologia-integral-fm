package com.odontologiaintegralfm.feature.appointment.core.dto;

import java.time.LocalDateTime;

/**
 * DT
 */
public record AppointmentConflictResponseDTO(
        Long appointemntId,
        LocalDateTime appointemntDateTime,
        String patientName,
        String reasonKey,
        String reasonLabel

) {
}
