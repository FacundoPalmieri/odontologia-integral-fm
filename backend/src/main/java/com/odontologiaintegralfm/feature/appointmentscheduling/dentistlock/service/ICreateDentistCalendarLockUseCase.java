package com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.service;

import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.dto.DentistCalendarLockRequestCreateDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.dto.DentistCalendarLockResponseDTO;
import com.odontologiaintegralfm.shared.dto.Response;


public interface ICreateDentistCalendarLockUseCase {


    /**
     * Método para crear un bloqueo de calendario para el dentista.
     * @param idPerson : id Persona
     * @param dentistCalendarLockRequestCreateDTO : Dto request.
     */
    Response<DentistCalendarLockResponseDTO> execute(Long idPerson, DentistCalendarLockRequestCreateDTO dentistCalendarLockRequestCreateDTO);


    /**
     * Método para simular un bloqueo de calendario, lo que permite detectar posibles conflictos con turnos.
     * @param idPerson  : Id Dentista.
     * @param dentistCalendarLockRequestCreateDTO : Datos del evento.
     */
    Response<DentistCalendarLockResponseDTO> executePreview(Long idPerson, DentistCalendarLockRequestCreateDTO dentistCalendarLockRequestCreateDTO);





}
