package com.odontologiaintegralfm.feature.appointment.core.dto;


import com.odontologiaintegralfm.feature.appointment.core.enums.AppointmentStatus;
import java.time.LocalDateTime;

public record AppointmentCreateResponseDTO(
        Long id,
        String dentistName,
        String patientName,
        LocalDateTime appointmentDateTime,
        AppointmentStatus status
) {
}
