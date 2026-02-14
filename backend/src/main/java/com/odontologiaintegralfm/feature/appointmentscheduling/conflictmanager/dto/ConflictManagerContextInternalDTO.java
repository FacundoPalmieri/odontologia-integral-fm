package com.odontologiaintegralfm.feature.appointmentscheduling.conflictmanager.dto;

import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.model.Appointment;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.model.AppointmentConflict;
import java.util.List;

/**
 * DTO interno del servicio de Conflict Manager.
 * Se utiliza para preparar el contexto de validación de turnos y turnos en conflicto.
 */
public record ConflictManagerContextInternalDTO(
        List<Appointment> appointments,
        List<AppointmentConflict> appointmentConflicts
) {

    public static ConflictManagerContextInternalDTO build(List<Appointment> appointments, List<AppointmentConflict> appointmentConflicts){
        return new ConflictManagerContextInternalDTO(appointments, appointmentConflicts);
    }
}
