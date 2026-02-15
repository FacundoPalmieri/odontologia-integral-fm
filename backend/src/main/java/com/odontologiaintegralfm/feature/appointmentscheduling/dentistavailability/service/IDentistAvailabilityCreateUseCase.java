package com.odontologiaintegralfm.feature.appointmentscheduling.dentistavailability.service;


import com.odontologiaintegralfm.feature.appointmentscheduling.dentistavailability.dto.DentistAvailabilityResponseDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistavailability.dto.WorkingDayDTO;
import com.odontologiaintegralfm.shared.dto.Response;

import java.util.List;

public interface IDentistAvailabilityCreateUseCase {

    /**
     * Método para crear una nueva jornada laboral de un dentista.
     * - Inicio de jornada.
     * - Fin de jornada.
     * - Duración de turno.
     * @param days : DTO con datos de parametrización de la jornada.
     */
    Response<DentistAvailabilityResponseDTO> execute(Long id, List<WorkingDayDTO> days);



    /**
     * Método para simular una nueva jornada laboral de un dentista.
     *
     * @param id : id dentista
     * @param days : Lista de jornadas.
     */
    Response<DentistAvailabilityResponseDTO> executePreview(Long id, List<WorkingDayDTO> days);


}
