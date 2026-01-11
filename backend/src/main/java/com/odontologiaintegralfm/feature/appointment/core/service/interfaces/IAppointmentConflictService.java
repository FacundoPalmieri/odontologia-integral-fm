package com.odontologiaintegralfm.feature.appointment.core.service.interfaces;

import com.odontologiaintegralfm.feature.appointment.core.dto.AppointmentConflictResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.enums.OriginConflict;
import com.odontologiaintegralfm.feature.appointment.core.model.Appointment;
import com.odontologiaintegralfm.feature.appointment.core.model.AppointmentConflict;
import com.odontologiaintegralfm.feature.user.model.UserSec;
import com.odontologiaintegralfm.shared.dto.Response;

import java.time.LocalDateTime;
import java.util.List;


public interface IAppointmentConflictService {


    /**
     * Obtiene la lista de turnos conflictivos (Resueltos o no) por ID de dentista y afectados por jornada laboral.
     * @param idDentist: id Dentista.
     */
    List<AppointmentConflict> getAllByDentistIdAndAvailabilityConflict(Long idDentist, OriginConflict originConflict);


    /**
     *
     * Obtiene la lista de turnos conflictivos NO RESUELTOS por ID de dentista y por ID de bloqueo de agenda.
     * @param idDentist     : id Dentista.
     * @param lockConflictId: id bloqueo de agenda.
     */
    List<AppointmentConflict> getAllByDentistIdAndCalendarLockConflictId(Long idDentist, Long lockConflictId);




    /**
     * Devuelve una Response con Lista todos los conflictos del dentista.
     */
    Response<List<AppointmentConflictResponseDTO>> getConflictByDentistId(Long idDentist);




    /**
     *
     * Obtiene turno con en conflicto por turno y estado de resolución
     * @param appointment : Turno
     * @param resolved: Estado de resolución.
     */
    AppointmentConflict findAppointmentConflictByAppointmentAndResolved(Appointment appointment, Boolean resolved);




    /**
     * Crea turnos en conflictos.
     * @param appointmentConflicts : Lista con turnos conflictivos.
     */
    List<AppointmentConflict> create(List<AppointmentConflict> appointmentConflicts);





    /**
     * Actualiza turnos conflictivo como resueltos
     * @param appointmentsIds : id Turnos en conflictos
     * @param updateAt : Fecha del día
     * @param updateBy : Usuario que actualiza.
     */
    void resolvedAll(List<Long> appointmentsIds, LocalDateTime updateAt, UserSec updateBy);





    /**
     * Actualiza un turno conflictivo
     * @param appointment: Turno
     */
    AppointmentConflict update(AppointmentConflict appointment);





}
