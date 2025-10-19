package com.odontologiaintegralfm.feature.appointment.core.service.interfaces;


import com.odontologiaintegralfm.feature.appointment.core.dto.DentistCalendarLockCreateRequestDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistCalendarLockResponseDTO;
import com.odontologiaintegralfm.shared.response.Response;

public interface IDentistLockCalendarService {

    /**
     * Método para crear una relación entre dentista y evento de bloqueo de agenda.
     * @param idDentist  : Id dentista.
     * @param dentistCalendarService : Datos del evento.
     */
    Response<DentistCalendarLockResponseDTO> create (Long idDentist, DentistCalendarLockCreateRequestDTO dentistCalendarService);
}
