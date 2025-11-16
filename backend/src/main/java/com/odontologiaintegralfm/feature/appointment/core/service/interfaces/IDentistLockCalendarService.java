package com.odontologiaintegralfm.feature.appointment.core.service.interfaces;



import com.odontologiaintegralfm.feature.appointment.core.dto.DentistCalendarLockRequestCreateDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistCalendarLockRequestUpdateDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistCalendarLockResponseDTO;
import com.odontologiaintegralfm.shared.response.Response;

public interface IDentistLockCalendarService {

    /**
     * Método para crear una relación entre dentista y evento de bloqueo de agenda.
     * @param idPerson  : Id Dentista.
     * @param dentistCalendarLockRequestCreateDTO : Datos del evento.
     */
    Response<DentistCalendarLockResponseDTO> create (Long idPerson, DentistCalendarLockRequestCreateDTO dentistCalendarLockRequestCreateDTO);

    /**
     * Método para actualizar una relación entre dentista y evento de bloqueo de agenda.
     * @param dentistCalendarLockRequestUpdateDTO : Datos del evento.
     */
    Response<DentistCalendarLockResponseDTO> update (DentistCalendarLockRequestUpdateDTO dentistCalendarLockRequestUpdateDTO);

}
