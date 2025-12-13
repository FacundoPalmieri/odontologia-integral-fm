package com.odontologiaintegralfm.feature.appointment.core.service.interfaces;

import com.odontologiaintegralfm.feature.appointment.core.dto.DentistAvailabilityResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.WorkingDayDTO;
import com.odontologiaintegralfm.feature.appointment.core.model.DentistAvailability;
import com.odontologiaintegralfm.shared.dto.Response;

import java.time.LocalDate;
import java.time.LocalTime;
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



    /**
     * Método privado que valída que la fecha de inicio y fin cubra al menos la parametrización de la duración de un turno.
     * @param idDentist: Id Dentista
     * @param startTime: Hora inicio jornada de feriado
     * @param endTime  : Hora fin jornada de feriado
     */
    boolean validateDurationLessThanAppointmentDuration(Long idDentist, LocalTime startTime, LocalTime endTime);


    /**
     * Método que verifica si una fecha dada es coincidente con la alguna jornada laboral de dentista.
     * @param dentist : id Dentist.
     * @param date : Fecha a consultar
     * @return : La jornada laboral.
     */
    DentistAvailability getDentistAvailabilityByDate(Long dentist, LocalDate date);
}
