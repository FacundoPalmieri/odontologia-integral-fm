package com.odontologiaintegralfm.feature.appointmentscheduling.locktype.service;


import com.odontologiaintegralfm.feature.appointmentscheduling.locktype.dto.CalendarLockModeResponseDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.locktype.dto.CalendarLockTypeCreateRequestDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.locktype.dto.CalendarLockTypeResponseDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.locktype.dto.CalendarLockTypeUpdateRequestDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.locktype.model.CalendarLockType;
import com.odontologiaintegralfm.shared.dto.Response;
import java.util.List;
import java.util.Set;

public interface ICalendarLockTypeService {

    /**
     * Obtener todos los tipos de bloqueos de agenda.
     */
    Response<List<CalendarLockTypeResponseDTO>> getAll();

    /**
     * Obtiene un tipo de bloqueo por su ID.
     */
    Response<CalendarLockTypeResponseDTO> getById(Long id);


    /**
     * Obtiene los diferentes modos para un tipo de bloqueo.
     */

    Response<Set<CalendarLockModeResponseDTO>> getModes();

    /**
     * Obtiene un tipo de bloqueo por su ID.
     * Método interno de validación. Este método es llamado desde el servicio de "DentistCalendarLockService" método "create"
     */
     CalendarLockType getByIdInternal(Long id);


    /**
     * Crear un nuevo tipo de bloqueo de agenda.
     */
    Response<CalendarLockTypeResponseDTO> create(CalendarLockTypeCreateRequestDTO calendarLockTypeCreateRequestDTO);

    /**
     * Actualizar un tipo de bloqueo de agenda.
     */
    Response<CalendarLockTypeResponseDTO> update(Long id, CalendarLockTypeUpdateRequestDTO calendarLockTypeCreateRequestDTO);

}
