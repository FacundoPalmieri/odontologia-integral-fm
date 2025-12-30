package com.odontologiaintegralfm.feature.appointment.core.service.interfaces;



import com.odontologiaintegralfm.feature.appointment.core.dto.DentistCalendarLockRequestCreateDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistCalendarLockRequestUpdateDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistCalendarLockResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.model.DentistCalendarLock;
import com.odontologiaintegralfm.shared.dto.Response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface IDentistCalendarLockService {

    /**
     * Método para crear una relación entre dentista y evento de bloqueo de agenda.
     * @param idPerson  : Id Dentista.
     * @param dentistCalendarLockRequestCreateDTO : Datos del evento.
     */
    Response<DentistCalendarLockResponseDTO> create (Long idPerson, DentistCalendarLockRequestCreateDTO dentistCalendarLockRequestCreateDTO);



    /**
     * Método para simular un bloqueo de calendario, lo que permite detectar posibles conflictos con turnos.
     * @param idPerson  : Id Dentista.
     * @param dentistCalendarLockRequestCreateDTO : Datos del evento.
     */
    Response<DentistCalendarLockResponseDTO> createPreview (Long idPerson, DentistCalendarLockRequestCreateDTO dentistCalendarLockRequestCreateDTO);





    /**
     * Método para actualizar una relación entre dentista y evento de bloqueo de agenda.
     * @param dentistCalendarLockRequestUpdateDTO : Datos del evento.
     */
    Response<DentistCalendarLockResponseDTO> update (DentistCalendarLockRequestUpdateDTO dentistCalendarLockRequestUpdateDTO);




    /**
     * Método para obtener todos los bloqueos que corresponde solo a una fecha dada.
     * @param dentistId : id dentista
     * @param date : fecha a consulta por bloqueo.
     */
    List<DentistCalendarLock> getByDate(Long dentistId, LocalDate date);




    /**
     * Valída si una fecha y hora se encuentran bloqueadas por un dentista.
     * Si existe, no realiza acción.
     * Si no existe, arroja exceptión.
     * @param idDentist : Id dentista
     * @param dateTime : Fecha y hora.
     */
    void validateByIdDentistAndDateTime(Long idDentist, LocalDateTime dateTime);


    /**
     * Valída que no exista ya un bloqueo de agenda con la misma configuración
     * (fecha inicio/fin, recurrencia y al menos un día en común).
     * @param idPerson : Id dentista.
     * @param dentistCalendarLockRequestCreateDTO : Objeto nuevo a crear.
     */
    void verifyLockMatchWithLock(Long idPerson, DentistCalendarLockRequestCreateDTO dentistCalendarLockRequestCreateDTO);
}
