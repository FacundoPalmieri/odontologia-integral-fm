package com.odontologiaintegralfm.feature.appointment.core.service.interfaces;

import com.odontologiaintegralfm.feature.appointment.core.dto.DentistAvailabilityResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.WorkingDayDTO;
import com.odontologiaintegralfm.feature.appointment.core.model.DentistAvailability;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.exception.BadRequestException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * @author [Facundo Palmieri]
 */
public interface IDentistAvailabilityService {

    /**
     * Método para crear una nueva jornada laboral de un dentista.
     * - Inicio de jornada.
     * - Fin de jornada.
     * - Duración de turno.
     * @param days : DTO con datos de parametrización de la jornada.
     */
    Response<DentistAvailabilityResponseDTO> create(Long id, List<WorkingDayDTO> days);


    /**
     * Método para simular una nueva jornada laboral de un dentista.
     */
    Response<DentistAvailabilityResponseDTO> createPreview(Long id, List<WorkingDayDTO> days);


    /**
     * Método para obtener la disponibilidad de un dentista.
     * - Inicio de jornada.
     * - Fin de jornada.
     * - Duración de turno.
     * @param id: Id dentista
     */
    Response<DentistAvailabilityResponseDTO> get(Long id);


    /**
     * Método para obtener las jornadas laborales de un dentista.
     * @param idDentist: Id dentista.
     */
    List<DentistAvailability> getByIdInternal(Long idDentist);


    /**
     * Método para obtener el tiempo de duración de un turno por ID de dentista.
     */
    Integer getAppointmentDuration(Long idDentist);




    /**
     * Método que verifica si una fecha dada es coincidente con la alguna jornada laboral de dentista.
     * @param dentist : id Dentist.
     * @param date : Fecha a consultar
     * @param dentistAvailabilities : Lista de disponibilidades laborales.
     * @return : La jornada laboral.
     */
    DentistAvailability getDentistAvailabilityByDate(Long dentist, LocalDate date, List<DentistAvailability> dentistAvailabilities);



    /**
     * Valída que un bloqueo de calendario propuesto coincida con la jornada laboral del dentista.
     * @param idDentist Id del dentista cuyo calendario se valida.
     * @param blocksDate Fechas de bloqueos
     * @param starTimeBlock Hora de inicio del bloqueo.
     * @param endTimeBlock Hora de fin del bloqueo.
     * @param fullDay indica si el bloqueo ocupa toda la jornada.
     * @throws BadRequestException Si el bloqueo no cumple con la cobertura requerida según la recurrencia y jornada del dentista.
     */
    void validateCoverage(Long idDentist, List<LocalDate> blocksDate, LocalTime starTimeBlock, LocalTime endTimeBlock, boolean fullDay);




    /**
     * Valída que una fecha/hora esté dentro de la jornada laboral del dentista.
     * @param idDentist : idDentista
     * @param appointmentDateTime : Fecha y hora a evaluar.
     */
    void isDateTimeWithinAvailability(Long idDentist, LocalDateTime appointmentDateTime);
}
