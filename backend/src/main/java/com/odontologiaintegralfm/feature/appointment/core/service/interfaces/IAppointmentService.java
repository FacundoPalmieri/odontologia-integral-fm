package com.odontologiaintegralfm.feature.appointment.core.service.interfaces;

import com.odontologiaintegralfm.feature.appointment.core.model.Appointment;
import java.util.List;


public interface IAppointmentService {

    /**
     * Método para obtener turnos en estado "Reservado"
     * @param idDentist : id Dentista.
     * @return : Lista de turnos.
     */
    List<Appointment> getAppointmentsReservedByDentistInternal(Long idDentist);




}
