package com.odontologiaintegralfm.feature.appointment.core.service.interfaces;



import com.odontologiaintegralfm.feature.appointment.core.dto.DentistCalendarLockRequestCreateDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistCalendarLockRequestUpdateDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistCalendarLockResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.model.DentistCalendarLock;
import com.odontologiaintegralfm.shared.dto.Response;

import java.time.LocalDate;
import java.util.List;

public interface IDentistCalendarLockService {

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



    /**
     * Método para obtener todos los bloqueos con fecha de fin mayor al día actual.
     * @param dentistId : id dentista
     */
   List<DentistCalendarLock> getAllCurrentByDentistId(Long dentistId);




    /**
     * Método para obtener todos los bloqueos verificando que el inicio sea <= y el fin sea => a una fecha dada.
     * @param dentistId : id dentista
     */
    List<DentistCalendarLock> getByDentistIdAndDateRange(Long dentistId, LocalDate date);



    /**
     * Método para obtener todos los bloqueos que corresponde solo a una fecha dada.
     * @param dentistId : id dentista
     * @param date : fecha a consulta por bloqueo.
     */
    List<DentistCalendarLock> getByDate(Long dentistId, LocalDate date);


}
