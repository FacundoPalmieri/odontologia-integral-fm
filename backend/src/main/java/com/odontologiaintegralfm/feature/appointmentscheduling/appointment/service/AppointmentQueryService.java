package com.odontologiaintegralfm.feature.appointmentscheduling.appointment.service;

import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.model.Appointment;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.enums.AppointmentStatus;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.repository.IAppointmentRepository;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AppointmentQueryService {


    private final IAppointmentRepository appointmentRepository;


    public AppointmentQueryService(
            IAppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }


    /**
     * Obtiene la lista de turnos de un día para un dentista específico.
     * @param idDentist : id Dentista.
     * @param date : Fecha
     */
    public List<Appointment> getAppointmentByDentistAndDate(Long idDentist, LocalDate date, AppointmentStatus status) {
        try{
            return appointmentRepository.findByDentistIdAndDateBetweenAndStatus(idDentist,date.atStartOfDay(),date.plusDays(1).atStartOfDay(), status);
        }
        catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "AppointmentQueryService", idDentist, "<- ID Dentist", "getAppointmentByDentistAndDate");
        }
    }

    /**
     * Obtiene turnos de un dentista en estado Reservado en un período específico.
     *
     * @param idDentist : id Dentista
     * @param start     : Inicio del período
     * @param end       : Fin del período
     * @param status    : Estado del turno.
     * @return : Map Fecha -> Turno
     */
    public Map<LocalDate, List<Appointment>> getByDateRange(Long idDentist, LocalDateTime start, LocalDateTime end, AppointmentStatus status) {
        List<Appointment> appointments = appointmentRepository.findByDentistIdAndDateBetweenAndStatus(idDentist,start,end,status);

        Map<LocalDate, List<Appointment>> appointmentsByDate = appointments.stream()
                .collect(Collectors.groupingBy(
                        appointment -> appointment.getDate().toLocalDate()

                ));

        return appointmentsByDate;
    }


    public Appointment getById(Long idAppointment) {

        try{
            return appointmentRepository.findById(idAppointment)
                    .orElseThrow(() ->new NotFoundException("exception.appointment.notFound.user",null,"exception.appointment.notFound.log", new Object[]{idAppointment,"AppointmentQueryService","getById"},LogLevel.ERROR));        }
        catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "AppointmentQueryService", idAppointment, "<- ID Appointment", "getById");
        }

    }


    public List<Appointment> getFutureAppointmentsReservedByDentist(Long idDentist) {
        return appointmentRepository.findFutureAppointmentsReservedByDentist(idDentist,LocalDateTime.now(), AppointmentStatus.RESERVED);
    }
}

