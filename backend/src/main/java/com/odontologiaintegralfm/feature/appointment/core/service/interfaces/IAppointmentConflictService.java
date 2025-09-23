package com.odontologiaintegralfm.feature.appointment.core.service.interfaces;

import com.odontologiaintegralfm.feature.appointment.core.model.AppointmentConflict;

import java.util.List;

/**
 * @author [Facundo Palmieri]
 */
public interface IAppointmentConflictService {


    /**
     * Obtiene la lista de turnos conflictivos por ID de dentista.
     * @param idDentist: id Dentista.
     */
    List<AppointmentConflict> getAllByDentist(Long idDentist);


    /**
     * Crea turnos en conflictos.
     * @param appointmentConflicts : Lista con turnos conflictivos.
     */
    List<AppointmentConflict> create(List<AppointmentConflict> appointmentConflicts);

    /**
     * Actualiza  turnos conflictivo
     * @param appointmentConflicts: Turno
     */
    List<AppointmentConflict> update(List<AppointmentConflict> appointmentConflicts);



    /**
     * Actualiza un turno conflictivo
     * @param appointment: Turno
     */
    AppointmentConflict update(AppointmentConflict appointment);

}
