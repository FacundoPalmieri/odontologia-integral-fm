package com.odontologiaintegralfm.feature.appointment.core.service.interfaces;


import com.odontologiaintegralfm.feature.appointment.core.dto.AppointmentConflictResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistCalendarLockRequestCreateDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.WorkingDayDTO;
import com.odontologiaintegralfm.feature.appointment.core.model.Appointment;
import com.odontologiaintegralfm.feature.appointment.core.model.AppointmentConflict;
import com.odontologiaintegralfm.feature.appointment.core.model.DentistCalendarLock;
import com.odontologiaintegralfm.feature.dentist.core.model.Dentist;

import java.util.List;

public interface IConflictManagerService {

    /**
     * Método para verificar conflictos antes cambios en la jornada laboral del dentista.4
     * @param idDentist : id Dentista.
     * @param days : Lista con DTOs qie tienen la nueva jornada laboral.
     * @return : Lista de AppointmentConflictResponseDTO
     */
    List<AppointmentConflictResponseDTO> verifyConflictsByDentistAvailability(Long idDentist, List<WorkingDayDTO> days);


    /**
     * Método para verificar conflictos antes bloqueos de calendario del dentista.
     * @param dentistCalendarLockRequestCreateDTO
     * @param dentists
     * @return
     */
    List<AppointmentConflictResponseDTO> verifyConflictsByDentistCalendarLock(DentistCalendarLockRequestCreateDTO dentistCalendarLockRequestCreateDTO, Dentist dentists);



    /**
     * Actualiza turnos en conflictos como resueltos.
     */
    void updateResolvedConflicts(List<AppointmentConflict> appointmentConflicts);


    /**
     * Resuelve los turnos en conflicto cuando un bloqueo de calendario se finaliza anticipadamente.
     * <p>
     * El método obtiene todos los conflictos asociados al bloqueo y, si existen, los marca como resueltos
     * mediante el servicio de gestión de conflictos.
     *
     * @param dentistCalendarLock Bloqueo de calendario que se está finalizando anticipadamente.
     */
    void resolvedAppointmentConflictByFinishLock(DentistCalendarLock dentistCalendarLock);





}

