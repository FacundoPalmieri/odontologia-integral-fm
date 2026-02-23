package com.odontologiaintegralfm.feature.appointmentscheduling.appointment.service;

import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.dto.AppointmentConflictResponseDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.calendar.enums.OriginConflict;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.model.Appointment;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.model.AppointmentConflict;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.repository.IAppointmentConflictRepository;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.model.DentistCalendarLock;
import com.odontologiaintegralfm.feature.user.model.UserSec;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import com.odontologiaintegralfm.shared.dto.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class AppointmentConflictService implements IAppointmentConflictService {

    @Autowired
    private IAppointmentConflictRepository appointmentConflictRepository;
    @Qualifier("messageSource")
    @Autowired
    private MessageSource messageSource;


    /**
     * Obtiene la lista de turnos conflictivos (Resueltos o no) por ID de dentista y afectados por jornada laboral.
     *
     * @param idDentist      : id Dentista.
     * @param originConflict
     */
    @Override
    public List<AppointmentConflict> getAllByDentistIdAndAvailabilityConflict(Long idDentist, OriginConflict originConflict) {
        return appointmentConflictRepository.findAllByDentistAndOriginConflict(idDentist,originConflict, LocalDateTime.now());
    }

    /**
     * Obtiene la lista de turnos conflictivos NO RESUELTOS por ID de dentista y por ID de bloqueo de agenda.
     *
     * @param idDentist      : id Dentista.
     * @param lockConflictId : id bloqueo de agenda.
     */
    @Override
    public List<AppointmentConflict> getAllByDentistIdAndCalendarLockConflictId(Long idDentist, Long lockConflictId) {
        return appointmentConflictRepository.findAllByDentistIdAndIdOriginConflict(idDentist,lockConflictId);
    }





    /**
     * Devuelve una Response con Lista todos los conflictos del dentista.
     */
    @Override
    public Response<List<AppointmentConflictResponseDTO>> getConflictByDentistId(Long idDentist) {

        List<AppointmentConflict> appointmentConflicts = appointmentConflictRepository.findByIdDentistAndResolvedFalse(idDentist);

        if ( appointmentConflicts == null || appointmentConflicts.isEmpty()) {
            String messageUser = messageSource.getMessage("appointmentService.getConflict.empty", null, LocaleContextHolder.getLocale());
            return new Response<>(false, messageUser, Collections.emptyList());
        }

        List<AppointmentConflictResponseDTO> conflicts = appointmentConflicts.stream()
                .map(AppointmentConflictResponseDTO::build)
                .toList();
        return new Response<>(true, "", conflicts);
    }

    /**
     * Obtiene turno con en conflicto turno y estado de resolución.
     *
     * @param appointment  : Turno
     * @param resolved: Estado de resolución.
     */
    @Override
    public AppointmentConflict findAppointmentConflictByAppointmentAndResolved(Appointment appointment, Boolean resolved){

        return appointmentConflictRepository.findAppointmentConflictByAppointmentAndResolved(appointment,resolved);


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
     * Actualiza turnos conflictivo como resueltos
     * @param appointmentsIds : id Turnos en conflictos
     * @param updateAt : Fecha del día
     * @param updateBy : Usuario que actualiza.
     */
    @Override
    public void resolvedAll(List<Long> appointmentsIds, LocalDateTime updateAt, UserSec updateBy) {
        try{
             appointmentConflictRepository.resolved(appointmentsIds,updateAt,updateBy);

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




    /**
     * Resuelve los turnos en conflicto cuando un bloqueo de calendario se finaliza anticipadamente.
     * <p>
     * El método obtiene todos los conflictos asociados al bloqueo y, si existen, los marca como resueltos
     * mediante el servicio de gestión de conflictos.
     *
     * @param dentistCalendarLock Bloqueo de calendario que se está finalizando anticipadamente.
     */
    @Override
    public void resolvedAppointmentConflictByFinishLock(DentistCalendarLock dentistCalendarLock, UserSec userSec) {
        List<AppointmentConflict> appointmentConflicts = appointmentConflictRepository.findAllByDentistIdAndIdOriginConflict(dentistCalendarLock.getDentist().getId(), dentistCalendarLock.getId());

        if(!appointmentConflicts.isEmpty()){
            this.updateResolvedConflicts(appointmentConflicts, userSec);
        }
    }




    /**
     * Marca como resueltos los conflictos de turnos de un dentista recibida
     * @param appointmentConflicts Lista de {@link AppointmentConflict} que indica los turnos que deben marcarse como resueltos.
     */
    @Override
    public void updateResolvedConflicts(List<AppointmentConflict> appointmentConflicts, UserSec userSec) {

        List<Long> ids = appointmentConflicts.stream()
                .map(AppointmentConflict::getId)
                .toList();

        resolvedAll(ids, LocalDateTime.now(), userSec);

    }


}
