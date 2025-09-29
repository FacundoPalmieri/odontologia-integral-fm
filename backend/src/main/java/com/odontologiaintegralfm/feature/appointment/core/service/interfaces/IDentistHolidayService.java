package com.odontologiaintegralfm.feature.appointment.core.service.interfaces;

import com.odontologiaintegralfm.feature.appointment.core.dto.DentistHolidayRequestDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistHolidayResponseDTO;
import com.odontologiaintegralfm.shared.response.Response;

/**
 * @author [Facundo Palmieri]
 */
public interface IDentistHolidayService {

    /**
     * Método para la creación de la relación de un dentista con feriados.
     */
    Response<DentistHolidayResponseDTO> createOrUpdate(Long idDentist,DentistHolidayRequestDTO dentistHolidayRequestDTO);


    /**
     * Método para obtener la relación entre un dentista y los feriados.
     * @param idDentist : Id Dentista
     * @param year      : Año consultado
     */
    Response<DentistHolidayResponseDTO> get(Long idDentist, Integer year);

}
