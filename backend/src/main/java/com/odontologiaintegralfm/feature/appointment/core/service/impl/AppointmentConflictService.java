package com.odontologiaintegralfm.feature.appointment.core.service.impl;

import com.odontologiaintegralfm.feature.appointment.core.model.AppointmentConflict;
import com.odontologiaintegralfm.feature.appointment.core.repository.IAppointmentConflictRepository;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.IAppointmentConflictService;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentConflictService implements IAppointmentConflictService {

    @Autowired
    private IAppointmentConflictRepository appointmentConflictRepository;


    /**
     * Obtiene la lista de turnos conflictivos por ID de dentista.
     *
     * @param idDentist : id Dentista.
     */
    @Override
    public List<AppointmentConflict> getAllByDentist(Long idDentist) {
        try{
            return appointmentConflictRepository.findAllByIdDentist(idDentist, LocalDateTime.now());

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "AppointmentConflictService", idDentist,"<-- ID DENTISTA", "getAllByDentist");
        }
    }

    /**
     * Obtiene la lista de turnos conflictivos NO RESUELTOS por id de dentista.
     */
    @Override
    public List<AppointmentConflict> getAllNotResolvedByDentist(Long idDentist) {
        try{
            return appointmentConflictRepository.findByIdDentistAndResolvedFalse(idDentist);
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "AppointmentConflictService", idDentist, null, "getAllNotResolvedByDentist");
        }
    }

    /**
     * Crea turnos en conflictos.
     * @param appointmentConflicts : Lista con turnos conflictivos.
     */
    @Override
    public List<AppointmentConflict> create(List<AppointmentConflict> appointmentConflicts) {
        try{
            return appointmentConflictRepository.saveAll(appointmentConflicts);

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "AppointmentConflictService", null, null, "create");
        }
    }

    /**
     * Actualiza turnos conflictivo
     * @param appointmentConflicts : Turno
     */
    @Override
    public List<AppointmentConflict> update(List<AppointmentConflict> appointmentConflicts) {
        try{
            return appointmentConflictRepository.saveAll(appointmentConflicts);

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "AppointmentConflictService",null, null, "update");
        }
    }

    /**
     * Actualiza un turno conflictivo
     * @param appointment: Turno
     */
    @Override
    public AppointmentConflict update(AppointmentConflict appointment) {
        try{
            return appointmentConflictRepository.save(appointment);

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "AppointmentConflictService", appointment.getId(), null, "update");
        }
    }
}
