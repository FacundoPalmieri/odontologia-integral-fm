package com.odontologiaintegralfm.feature.appointment.catalogs.service;

import com.odontologiaintegralfm.feature.appointment.catalogs.dto.HolidayCreateRequestDTO;
import com.odontologiaintegralfm.feature.appointment.catalogs.dto.HolidayResponseDTO;
import com.odontologiaintegralfm.feature.appointment.catalogs.dto.HolidayUpdateRequestDTO;
import com.odontologiaintegralfm.feature.appointment.catalogs.model.Holiday;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistHolidayListRequestDTO;
import com.odontologiaintegralfm.infrastructure.scheduler.dto.internal.SchedulerResultDTO;
import com.odontologiaintegralfm.shared.response.Response;

import java.util.List;


public interface IHolidayService {

    /**
     * Método para obtener la lista de todos los feriados y devolverlos al cliente.
     * @param year      : Año a consultar
     */
    Response<List<HolidayResponseDTO>> getAll(int year);



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
     * Método para validar si existen los feriados dentro de una lista.
     * @param holidays: Lista de feriados a validar.
     * @param year    : Año
     */
    List<Holiday> validateHolidaysExist(List<DentistHolidayListRequestDTO> holidays, int year);


    /**
     * Método que consume ArgentinaDatosClient.
     * Mapea la respuesta recibida de la API a la entidad y persiste en BD.
     * @param year año a consultar
     */
    SchedulerResultDTO loadHolidays(int year);

}
