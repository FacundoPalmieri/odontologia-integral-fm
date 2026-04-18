package com.odontologiaintegralfm.feature.appointmentscheduling.dentistholiday.service;

import com.odontologiaintegralfm.feature.appointmentscheduling.dentistholiday.dto.DentistHolidayRequestCreateDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistholiday.dto.DentistHolidayResponseDTO;
import com.odontologiaintegralfm.shared.dto.Response;


public interface ICreateDentistHolidayUseCase {

    /**
     * Método para crear una relación entre "Dentista-Feriado".
     * @param userId: Id usuario
     * @param dto : dto request.
     */
    Response<DentistHolidayResponseDTO> execute(Long userId, DentistHolidayRequestCreateDTO dto);
}
