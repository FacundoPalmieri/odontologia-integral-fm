package com.odontologiaintegralfm.feature.appointment.core.dto;


import com.odontologiaintegralfm.feature.appointment.core.enums.AppointmentStatus;
import java.time.LocalDateTime;

public record AppointmentResponseDTO(
        Long id,
        String dentistName,
        String patientName,
        LocalDateTime appointmentDateTime,
        AppointmentStatus status
) {
}
