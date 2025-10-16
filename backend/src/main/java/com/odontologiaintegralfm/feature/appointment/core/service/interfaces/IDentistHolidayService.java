package com.odontologiaintegralfm.feature.appointment.core.service.interfaces;

import com.odontologiaintegralfm.feature.appointment.core.dto.DentistHolidayRequestCreateDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistHolidayRequestUpdateDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistHolidayResponseDTO;
import com.odontologiaintegralfm.shared.response.Response;

/**
 * @author [Facundo Palmieri]
 */
public interface IDentistHolidayService {

    /**
     * Método que crea relación entre dentista y feriado.
     */
    Response<DentistHolidayResponseDTO> create(Long idUser, DentistHolidayRequestCreateDTO dentistHolidayRequestCreateDTO);

    /**
     * Método para la actualización de la relación de un dentista con feriados.
     */
    Response<DentistHolidayResponseDTO> update(DentistHolidayRequestUpdateDTO dentistHolidayRequestUpdateDTO);

}
