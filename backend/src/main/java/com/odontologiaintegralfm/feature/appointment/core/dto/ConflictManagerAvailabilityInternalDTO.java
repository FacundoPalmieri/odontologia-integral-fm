package com.odontologiaintegralfm.feature.appointment.core.dto;

import com.odontologiaintegralfm.feature.appointment.core.model.Appointment;
import com.odontologiaintegralfm.feature.appointment.core.model.AppointmentConflict;
import java.util.List;

/**
 * DTO interno del servicio de Conflict Manager.
 * Se utiliza para preparar el contexto de validación de turnos y turnos en conflicto.
 */
public record ConflictManagerAvailabilityInternalDTO(
        List<Appointment> appointments,
        List<AppointmentConflict> appointmentConflicts
) {

    public static ConflictManagerAvailabilityInternalDTO build(List<Appointment> appointments,  List<AppointmentConflict> appointmentConflicts){
        return new ConflictManagerAvailabilityInternalDTO(appointments, appointmentConflicts);
    }
}
