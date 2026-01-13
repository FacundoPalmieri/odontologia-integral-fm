package com.odontologiaintegralfm.feature.appointment.core.dto;


import com.odontologiaintegralfm.feature.appointment.core.enums.AppointmentStatus;
import com.odontologiaintegralfm.feature.appointment.core.model.Appointment;

import java.time.LocalDateTime;

public record AppointmentResponseDTO(
        Long id,
        String dentistName,
        Long idPatient,
        String patientName,
        LocalDateTime appointmentDateTime,
        AppointmentStatus status
) {

    public static AppointmentResponseDTO build(Appointment appointment) {
        return new AppointmentResponseDTO(
                appointment.getId(),
                appointment.getDentist().getPerson().getLastName() + "," + appointment.getDentist().getPerson().getFirstName(),
                appointment.getPatient().getId(),
                appointment.getPatient().getPerson().getLastName() + "," + appointment.getPatient().getPerson().getFirstName(),
                appointment.getDate(),
                appointment.getStatus()
        );
    }
}
