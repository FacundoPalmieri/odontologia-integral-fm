package com.odontologiaintegralfm.feature.appointment.core.service.impl;

import com.odontologiaintegralfm.feature.appointment.core.dto.AppointmentConflictResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.enums.OriginConflict;
import com.odontologiaintegralfm.feature.appointment.core.model.AppointmentConflict;
import com.odontologiaintegralfm.feature.appointment.core.repository.IAppointmentConflictRepository;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.IAppointmentConflictService;
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
    public Response<List<AppointmentConflictResponseDTO>> getConflict(Long idDentist) {

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
}
