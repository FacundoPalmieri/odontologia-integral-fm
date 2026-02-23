package com.odontologiaintegralfm.feature.appointmentscheduling.dentistavailability.service;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.model.Appointment;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.model.AppointmentConflict;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistavailability.dto.DentistAvailabilityContextInternalDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistavailability.dto.DentistAvailabilityResponseDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistavailability.dto.WorkingDayDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistavailability.model.DentistAvailability;
import com.odontologiaintegralfm.feature.user.model.UserSec;
import com.odontologiaintegralfm.shared.dto.Response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author [Facundo Palmieri]
 */
public interface IDentistAvailabilityService {

    /**
     * Método de dominio para mapear, y persistir la entidad.
     */
    List<DentistAvailability> create( List<WorkingDayDTO> days,DentistAvailabilityContextInternalDTO availability, UserSec userSec);


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
     * Método para deshabilitar una disponibilidad
     * @param dentistAvailability : Lista de disponibilidades (días)
     * @param authenticatedUserService : Usuario autenticado.
     * @param now : Fecha y hora actual.
     */
    void disabledAvailability(List<DentistAvailability> dentistAvailability, AuthenticatedUserService authenticatedUserService, LocalDateTime now);


    /**
     * Método que verifica si una fecha dada es coincidente con la alguna jornada laboral de dentista.
     * @param dentist : id Dentist.
     * @param date : Fecha a consultar
     * @param dentistAvailabilities : Lista de disponibilidades laborales.
     * @return : La jornada laboral.
     */
    DentistAvailability getDentistAvailabilityByDate(Long dentist, LocalDate date, List<DentistAvailability> dentistAvailabilities);






    /**
     * Valída que una fecha/hora esté dentro de la jornada laboral del dentista.
     * @param idDentist : idDentista
     * @param appointmentDateTime : Fecha y hora a evaluar.
     */
    void isDateTimeWithinAvailability(Long idDentist, LocalDateTime appointmentDateTime);


    /**
     * Valída lo siguiente:
     * - Fin del break no puede ser anterior al inicio.
     * - Si un campo tiene datos, el otro también.
     */
    void validateBreak(List <DentistAvailability> dentistAvailability);





    /**
     * Detecta y genera conflictos de turnos que se encuentran fuera de la nueva jornada laboral de un dentista.
     * <p>
     * Este método compara cada turno futuro del dentista con la lista de {@link WorkingDayDTO} que define la nueva
     * disponibilidad laboral. Para cada turno que no se encuentra dentro de los días y horarios permitidos,
     * se genera un objeto {@link AppointmentConflict} indicando que está fuera de horario.
     * </p>
     *
     * @param appointments Lista de {@link Appointment} que representa los turnos futuros del dentista.
     * @param workingDays  Lista de {@link WorkingDayDTO} que define la nueva jornada laboral a evaluar.
     * @return Lista de {@link AppointmentConflict} representando los turnos que no se ajustan a la nueva jornada laboral.
     */
    List<AppointmentConflict> evaluateAppointmentDentistAvailability(List<Appointment> appointments, List<WorkingDayDTO> workingDays);

    /**
     * Método privado del servicio que permite mapea cada jornada laboral de la request a una entidad.
     * @param days : DTO con la jornada
     * @param dentistAvailabilityExisting : DTO interno del servicio que posea un dentista y una lista de disponibilidades.
     */
    List<DentistAvailability> entityFromDto(List<WorkingDayDTO> days, DentistAvailabilityContextInternalDTO dentistAvailabilityExisting, UserSec authenticatedUserService);

}
