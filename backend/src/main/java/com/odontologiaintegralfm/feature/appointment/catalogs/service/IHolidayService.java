package com.odontologiaintegralfm.feature.appointment.catalogs.service;

import com.odontologiaintegralfm.feature.appointment.catalogs.dto.HolidayCreateRequestDTO;
import com.odontologiaintegralfm.feature.appointment.catalogs.dto.HolidayResponseDTO;
import com.odontologiaintegralfm.feature.appointment.catalogs.dto.HolidayUpdateRequestDTO;
import com.odontologiaintegralfm.infrastructure.scheduler.dto.internal.SchedulerResultDTO;
import com.odontologiaintegralfm.shared.response.Response;
import org.springframework.data.domain.Page;

import java.util.List;


public interface IHolidayService {

    /**
     * Método para obtener la lista páginada de todos los feriados.
     * @param year      : Año a consultar.
     * @return
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
     * Método que consume ArgentinaDatosClient.
     * Mapea la respuesta recibida de la API a la entidad y persiste en BD.
     * @param year año a consultar
     */
    SchedulerResultDTO loadHolidays(int year);

}
