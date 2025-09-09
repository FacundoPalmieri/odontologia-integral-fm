package com.odontologiaintegralfm.feature.appointment.core.service.interfaces;

import com.odontologiaintegralfm.feature.appointment.core.dto.DentistAvailabilityRequestDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistAvailabilityResponseDTO;
import com.odontologiaintegralfm.shared.response.Response;

/**
 * @author [Facundo Palmieri]
 */
public interface IDentistAvailabilityService {

    /**
     * Método para crear la disponibilidad de turnos de un dentista.
     * - Inicio de jornada.
     * - Fin de jornada.
     * - Duración de turno.
     * @param dentistAvailabilityRequestDTO : DTO con datos de parametrización de la jornada.
     */
    Response<DentistAvailabilityResponseDTO> update(DentistAvailabilityRequestDTO dentistAvailabilityRequestDTO);

    /**
     * Método para obtener la disponibilidad de un dentista.
     * - Inicio de jornada.
     * - Fin de jornada.
     * - Duración de turno.
     * @param id: Id del dentista
     */
    Response<DentistAvailabilityResponseDTO> get(Long id);
}
