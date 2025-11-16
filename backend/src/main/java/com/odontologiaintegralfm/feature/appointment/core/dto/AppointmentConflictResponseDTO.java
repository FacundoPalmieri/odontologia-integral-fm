package com.odontologiaintegralfm.feature.appointment.core.dto;

import java.time.LocalDateTime;


public record AppointmentConflictResponseDTO(
        Long appointemntId,
        LocalDateTime appointemntDateTime,
        String patientName,
        Long idOriginConflict,
        String nameOriginConflict
) {
}
