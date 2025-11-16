package com.odontologiaintegralfm.feature.appointment.core.service.interfaces;

import com.odontologiaintegralfm.feature.appointment.catalogs.enums.DayName;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistAvailabilityResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.WorkingDayDTO;
import com.odontologiaintegralfm.feature.appointment.core.model.DentistAvailability;
import com.odontologiaintegralfm.shared.response.Response;

import java.util.List;

/**
 * @author [Facundo Palmieri]
 */
public interface IDentistAvailabilityService {

    /**
     * Método para crear la disponibilidad de turnos de un dentista.
     * - Inicio de jornada.
     * - Fin de jornada.
     * - Duración de turno.
     * @param days : DTO con datos de parametrización de la jornada.
     */
    Response<DentistAvailabilityResponseDTO> create(Long id, List<WorkingDayDTO> days);



    /**
     * Método para obtener la disponibilidad de un dentista.
     * - Inicio de jornada.
     * - Fin de jornada.
     * - Duración de turno.
     * @param id: Id del dentista
     */
    Response<DentistAvailabilityResponseDTO> get(Long id);


    /**
     * Método para obtener la jornada laboral de un dentista.
     * @param idDentist
     * @return
     */
    List<DentistAvailability> getByIdInternal(Long idDentist);


    /**
     * Método para obtener el tiempo de duración de un turno por ID de dentista.
     */
    Integer getAppointmentDuration(Long idDentist);
}
