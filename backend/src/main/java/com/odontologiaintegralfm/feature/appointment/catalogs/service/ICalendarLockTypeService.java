package com.odontologiaintegralfm.feature.appointment.catalogs.service;


import com.odontologiaintegralfm.feature.appointment.catalogs.dto.CalendarLockTypeCreateRequestDTO;
import com.odontologiaintegralfm.feature.appointment.catalogs.dto.CalendarLockTypeResponseDTO;
import com.odontologiaintegralfm.feature.appointment.catalogs.dto.CalendarLockTypeUpdateRequestDTO;
import com.odontologiaintegralfm.shared.response.Response;
import java.util.List;

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
     * Crear un nuevo tipo de bloqueo de agenda.
     */
    Response<CalendarLockTypeResponseDTO> create(CalendarLockTypeCreateRequestDTO calendarLockTypeCreateRequestDTO);

    /**
     * Actualizar un tipo de bloqueo de agenda.
     */
    Response<CalendarLockTypeResponseDTO> update(Long id, CalendarLockTypeUpdateRequestDTO calendarLockTypeCreateRequestDTO);

}
