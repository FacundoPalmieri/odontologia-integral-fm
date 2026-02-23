package com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.service;


import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.dto.DentistCalendarLockRequestUpdateDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.dto.DentistCalendarLockResponseDTO;
import com.odontologiaintegralfm.shared.dto.Response;

public interface IDentistCalendarLockUpdateUseCase {

    /**
     * Método para actualizar una relación entre dentista y evento de bloqueo de agenda.
     * @param dentistCalendarLockRequestUpdateDTO : Datos del evento.
     */
    Response<DentistCalendarLockResponseDTO> execute(DentistCalendarLockRequestUpdateDTO dentistCalendarLockRequestUpdateDTO);


}
