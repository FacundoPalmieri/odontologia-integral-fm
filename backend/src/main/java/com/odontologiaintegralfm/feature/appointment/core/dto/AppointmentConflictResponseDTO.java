package com.odontologiaintegralfm.feature.appointment.core.dto;

import com.odontologiaintegralfm.feature.appointment.core.model.AppointmentConflict;

import java.time.LocalDateTime;


public record AppointmentConflictResponseDTO(
        Long appointmentId,
        LocalDateTime appointmentDateTime,
        Long idPatient,
        String patientName,
        Long idOriginConflict,
        String nameOriginConflict
) {

    public static AppointmentConflictResponseDTO build(AppointmentConflict appointmentConflict) {
        return new AppointmentConflictResponseDTO(
                appointmentConflict.getAppointment().getId(),
                appointmentConflict.getAppointment().getDate(),
                appointmentConflict.getAppointment().getPatient().getId(),
                appointmentConflict.getAppointment().getPatient().getPerson().getLastName() + ", " + appointmentConflict.getAppointment().getPatient().getPerson().getFirstName(),
                appointmentConflict.getIdOriginConflict(),
                appointmentConflict.getOriginConflict().getLabel()

        );
    }
}
