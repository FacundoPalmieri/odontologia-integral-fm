package com.odontologiaintegralfm.feature.appointmentscheduling.appointment.service;

import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.dto.AppointmentConflictResponseDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.calendar.enums.OriginConflict;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.model.Appointment;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.model.AppointmentConflict;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.model.DentistCalendarLock;
import com.odontologiaintegralfm.feature.user.model.UserSec;
import com.odontologiaintegralfm.shared.dto.Response;
import org.springframework.security.core.userdetails.User;

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




    /**
     * Resuelve los turnos en conflicto cuando un bloqueo de calendario se finaliza anticipadamente.
     * <p>
     * El método obtiene todos los conflictos asociados al bloqueo y, si existen, los marca como resueltos
     * mediante el servicio de gestión de conflictos.
     *
     * @param dentistCalendarLock Bloqueo de calendario que se está finalizando anticipadamente.
     */
    void resolvedAppointmentConflictByFinishLock(DentistCalendarLock dentistCalendarLock, UserSec userSec);


    /**
     * Actualiza turnos en conflictos como resueltos.
     */
    void updateResolvedConflicts(List<AppointmentConflict> appointmentConflicts, UserSec userSec);
}
