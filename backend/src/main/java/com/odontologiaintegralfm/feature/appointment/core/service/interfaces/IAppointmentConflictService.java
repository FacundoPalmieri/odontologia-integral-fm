package com.odontologiaintegralfm.feature.appointment.core.service.interfaces;

import com.odontologiaintegralfm.feature.appointment.core.dto.AppointmentConflictResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.enums.OriginConflict;
import com.odontologiaintegralfm.feature.appointment.core.model.AppointmentConflict;
import com.odontologiaintegralfm.shared.dto.Response;

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
     * Obtiene la lista de turnos conflictivos NO RESUELTOS por Id de dentista.
     */
    List<AppointmentConflict> getNotResolved(Long idDentist);





    /**
     * Devuelve una Response con Lista todos los conflictos del dentista.
     */
    Response<List<AppointmentConflictResponseDTO>> getConflict(Long idDentist);




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
