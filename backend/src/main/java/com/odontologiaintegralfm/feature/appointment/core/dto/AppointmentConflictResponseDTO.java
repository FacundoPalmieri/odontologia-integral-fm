package com.odontologiaintegralfm.feature.appointment.core.dto;

import java.time.LocalDateTime;


public record AppointmentConflictResponseDTO(
        Long appointmentId,
        LocalDateTime appointmentDateTime,
        String patientName,
        Long idOriginConflict,
        String nameOriginConflict
) {
}
