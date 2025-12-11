package com.odontologiaintegralfm.feature.appointment.catalogs.service;

import com.odontologiaintegralfm.feature.appointment.catalogs.dto.HolidayCreateRequestDTO;
import com.odontologiaintegralfm.feature.appointment.catalogs.dto.HolidayResponseDTO;
import com.odontologiaintegralfm.feature.appointment.catalogs.dto.HolidayUpdateRequestDTO;
import com.odontologiaintegralfm.feature.appointment.catalogs.model.Holiday;
import com.odontologiaintegralfm.infrastructure.scheduler.dto.internal.SchedulerResultDTO;
import com.odontologiaintegralfm.shared.dto.Response;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface IHolidayService {

    /**
     * Método para obtener la lista de todos los feriados y devolverlos al cliente.
     * @param year      : Año a consultar
     */
    Response<List<HolidayResponseDTO>> getAll(int year);

    /**
     * Método interno de la aplicación para valida la existencia de un feriado.
     * @param id: id del feriado.
     */
    Holiday getByIdInternal(Long id);

    /**
     * Método para crear un feriado.
     * @param holidayCreateRequestDTO : DTO con el feriado a crear.
     */
    Response<HolidayResponseDTO> create(HolidayCreateRequestDTO holidayCreateRequestDTO);

    /**
     * Método para actualiza datos de un feriado.
     * @param holidayUpdateRequestDTO: DTO con el feriado a actualizar.
     */
    Response<HolidayResponseDTO> update(HolidayUpdateRequestDTO holidayUpdateRequestDTO);


    /**
     * Método para validar si existe un feriado por fecha
     */
    Optional<Holiday> getByDate(LocalDate date);



    /**
     * Método que consume ArgentinaDatosClient.
     * Mapea la respuesta recibida de la API a la entidad y persiste en BD.
     * @param year año a consultar
     */
    SchedulerResultDTO loadHolidays(int year);

}
