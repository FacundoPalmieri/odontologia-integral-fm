package com.odontologiaintegralfm.feature.appointment.core.service.impl;

import com.odontologiaintegralfm.feature.appointment.catalogs.enums.AppointmentStatus;
import com.odontologiaintegralfm.feature.appointment.core.model.Appointment;
import com.odontologiaintegralfm.feature.appointment.core.repository.IAppointmentRepository;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.IAppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AppointmentService implements IAppointmentService {

    @Autowired
    private IAppointmentRepository appointmentRepository;


    /**
     * Método protegido que sirve para obtener todos los turnos del dentista en estado "Reservado"
     * Se utiliza para validación interna de la aplicación
     */
    public List<Appointment> getAppointmentsReservedByDentistInternal(Long idDentist) {
        return appointmentRepository.findFutureAppointmentsReservedByDentist(idDentist, LocalDateTime.now(), AppointmentStatus.RESERVED);
    }


}

