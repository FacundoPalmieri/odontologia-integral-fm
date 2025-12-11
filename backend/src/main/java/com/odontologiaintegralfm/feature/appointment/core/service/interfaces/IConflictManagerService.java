package com.odontologiaintegralfm.feature.appointment.core.service.interfaces;


import com.odontologiaintegralfm.feature.appointment.core.enums.CalendarLockRecurrenceName;
import com.odontologiaintegralfm.feature.appointment.core.dto.AppointmentConflictResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistCalendarLockRequestCreateDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.WorkingDayDTO;
import com.odontologiaintegralfm.feature.appointment.core.model.Appointment;
import com.odontologiaintegralfm.feature.appointment.core.model.AppointmentConflict;
import com.odontologiaintegralfm.feature.appointment.core.model.DentistAvailability;
import com.odontologiaintegralfm.feature.dentist.core.model.Dentist;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface IConflictManagerService {

    /**
     * Método para verificar conflictos antes cambios en la jornada laboral del dentista.4
     * @param idDentist : id Dentista.
     * @param days : Lista con DTOs qie tienen la nueva jornada laboral.
     * @return : Lista de AppointmentConflictResponseDTO
     */
    List<AppointmentConflictResponseDTO> verifyConflictsByDentistAvailability(Long idDentist, List<WorkingDayDTO> days);


    /**
     * Método para verificar conflictos antes bloqueos de calendario del dentista.
     * @param dentistCalendarLockRequestCreateDTO
     * @param dentists
     * @return
     */
    List<AppointmentConflictResponseDTO> verifyConflictsByDentistCalendarLock(DentistCalendarLockRequestCreateDTO dentistCalendarLockRequestCreateDTO, Dentist dentists);


    /**
     * Actualiza turnos en conflictos como resueltos.
     * Recibe una lista de conflictos nuevos calculados vs. los existentes en la base de datos.
     * Si hay coincidencia los actualiza como resueltos.
     */
    void updateResolvedConflicts(Long dentistId,  List<AppointmentConflict> calculatedConflicts);


    /**
     * Método  detecta y genera nuevo conflictos de turnos
     * @param appointments : Lista de turnos futuros.
     * @param days         : Nueva jornada de trabajo.
     */
    List<AppointmentConflict> conflictDetectorByDentistAvailability(List<Appointment> appointments, List<WorkingDayDTO> days);


    /**
     * Verifica si un bloqueo propuesto en el calendario del dentista está completamente cubierto por sus disponibilidades.
     * @param availabilities Lista de {@link DentistAvailability} del dentista.
     * @param startDateBlock Fecha de inicio del bloqueo.
     * @param endDateBlock Fecha de fin del bloqueo.
     * @param startTimeBlock Hora de inicio del bloqueo.
     * @param endTimeBlock Hora de fin del bloqueo.
     * @param recurrenceBlock Recurrencia del bloqueo ({@link CalendarLockRecurrenceName}).
     * @return true si todas las fechas efectivas del bloqueo están cubiertas por alguna disponibilidad del dentista;
     *         false si al menos una fecha no tiene cobertura.
     */
    boolean hasBlockMatchWithAvailability(List<DentistAvailability> availabilities, LocalDate startDateBlock, LocalDate endDateBlock, LocalTime startTimeBlock, LocalTime endTimeBlock, CalendarLockRecurrenceName recurrenceBlock);




    /**
     * Obtiene la fecha REAL de inicio de un evento recurrente (por día de la semana)
     * a partir de una nueva jornada o bloqueo.
     * Busca, dentro de los próximos 7 días contando desde {@code startDate},
     * la primera fecha cuyo día de la semana coincida con {@code daysBlock}.
     *
     * @param startDate Fecha efectiva del cambio (normalmente el día posterior a la modificación del usuario).
     * @param daysBlock Día de la semana que representa la recurrencia (Lunes, Martes, etc.).
     * @return La primera fecha dentro de los próximos 7 días que coincide con el día indicado.
     */
    LocalDate findFirstMatchingDate(LocalDate startDate, DayOfWeek daysBlock);

    /**
     * Obtiene la fecha REAL de fin de un evento recurrente (por día de la semana)
     * a partir de una nueva jornada o bloqueo.
     *
     * Busca, dentro de los últimos 7 días contando desde {@code endDate},
     * la última fecha cuyo día de la semana coincida con {@code daysBlock}.
     *
     * @param endDate Fecha efectiva del cambio (normalmente el día posterior a la modificación del usuario).
     * @param daysBlock Día de la semana que representa la recurrencia (Lunes, Martes, etc.).
     * @return La última fecha dentro de los últimos 7 días que coincide con el día indicado.
     */
    LocalDate findLastMatchingDate(LocalDate endDate, DayOfWeek daysBlock);





    /**
     * Valída si una fecha específica coincide con un patrón de recurrencia a partir de una fecha de inicio.
     * <p>
     * Este método determina si {@code currentDate} cumple con la recurrencia definida en {@code recurrence}
     * tomando como referencia {@code startDate}. Se utiliza para validar bloqueos y disponibilidades recurrentes.
     * </p>
     * @param recurrence Tipo de recurrencia ({@link CalendarLockRecurrenceName}).
     * @param startDate Fecha de inicio real(no el día posterior) que sirve como referencia para la recurrencia.
     * @param currentDate Fecha que se desea validar contra la recurrencia.
     * @return true si currentDate cumple con la recurrencia definida respecto a startDate; false en caso contrario.
     */
    boolean validateRecurrence(CalendarLockRecurrenceName recurrence, LocalDate startDate, LocalDate currentDate);


}

