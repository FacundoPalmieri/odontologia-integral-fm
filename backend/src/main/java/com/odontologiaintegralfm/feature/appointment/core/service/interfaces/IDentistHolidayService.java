package com.odontologiaintegralfm.feature.appointment.core.service.interfaces;

import com.odontologiaintegralfm.feature.appointment.catalogs.model.Holiday;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistHolidayRequestDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistHolidayResponseDTO;
import com.odontologiaintegralfm.shared.response.Response;

import java.util.List;

/**
 * @author [Facundo Palmieri]
 */
public interface IDentistHolidayService {

    /**
     * Método que crea las relaaciones entre dentistas y feriados.
     * El mismo se ejecuta dentro de la tarea programada anual de carga de feriados.
     */
   void create(int year,List<Holiday> holidayList);

    /**
     * Método para la actualización de la relación de un dentista con feriados.
     */
    Response<DentistHolidayResponseDTO> update(Long idDentist, DentistHolidayRequestDTO dentistHolidayRequestDTO);


    /**
     * Método para obtener la relación entre un dentista y los feriados.
     * @param idDentist : Id Dentista
     * @param year      : Año consultado
     */
    Response<DentistHolidayResponseDTO> get(Long idDentist, Integer year);

}
