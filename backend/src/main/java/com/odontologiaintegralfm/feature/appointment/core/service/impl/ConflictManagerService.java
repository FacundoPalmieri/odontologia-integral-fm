package com.odontologiaintegralfm.feature.appointment.core.service.impl;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.appointment.core.enums.AppointmentStatus;
import com.odontologiaintegralfm.feature.appointment.core.enums.CalendarLockRecurrenceName;
import com.odontologiaintegralfm.feature.appointment.catalogs.enums.DayName;
import com.odontologiaintegralfm.feature.appointment.core.dto.AppointmentConflictResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistCalendarLockRequestCreateDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.WorkingDayDTO;
import com.odontologiaintegralfm.feature.appointment.core.enums.OriginConflict;
import com.odontologiaintegralfm.feature.appointment.core.model.Appointment;
import com.odontologiaintegralfm.feature.appointment.core.model.AppointmentConflict;
import com.odontologiaintegralfm.feature.appointment.core.model.DentistCalendarLock;
import com.odontologiaintegralfm.feature.appointment.core.repository.IAppointmentRepository;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.IAppointmentConflictService;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.IConflictManagerService;
import com.odontologiaintegralfm.feature.appointment.core.util.CalendarUtils;
import com.odontologiaintegralfm.feature.authentication.enums.Role;
import com.odontologiaintegralfm.feature.dentist.core.model.Dentist;
import com.odontologiaintegralfm.feature.user.service.IUserService;
import com.odontologiaintegralfm.infrastructure.email.service.IEmailService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.odontologiaintegralfm.feature.appointment.core.enums.CalendarLockRecurrenceName.DAILY;

/**
 * Servicio que centraliza la lógica para detectar y gestionar conflictos de turnos
 * cuando cambian la disponibilidad del odontólogo o se crean bloqueos en el calendario.
 * Contiene métodos para detectar conflictos nuevos, comparar con los ya existentes,
 * marcar conflictos como resueltos o reabrirlos y generar DTOs de respuesta.
 */
@Service
public class ConflictManagerService implements IConflictManagerService {

    private final AuthenticatedUserService authenticatedUserService;
    private final IAppointmentConflictService appointmentConflictService;
    private final IAppointmentRepository appointmentRepository;
    private final MessageSource messageSource;
    private final IUserService userService;
    private final IEmailService emailService;

    public ConflictManagerService(
            AuthenticatedUserService authenticatedUserService,
            IAppointmentConflictService appointmentConflictService,
            IAppointmentRepository appointmentRepository,
            @Qualifier("messageSource") MessageSource messageSource,
            IUserService userService,
            IEmailService emailService
    ) {
        this.authenticatedUserService = authenticatedUserService;
        this.appointmentConflictService = appointmentConflictService;
        this.appointmentRepository = appointmentRepository;
        this.messageSource = messageSource;
        this.userService = userService;
        this.emailService = emailService;
    }


    /**
     * Verifica y gestiona los conflictos de turnos futuros de un dentista ante cambios en su jornada laboral.
     * <p>
     * Este método detecta inconsistencias entre los turnos programados y la nueva disponibilidad configurada
     * para el dentista. Realiza una reevaluación completa de los turnos futuros, comparando los conflictos
     * recién detectados con los ya existentes en la base de datos, actualizando su estado según corresponda.
     * </p>
     *
     * <p>Flujo general:</p>
     * <ol>
     *   <li>Obtiene los turnos futuros del dentista.</li>
     *   <li>Obtiene los conflictos existentes cuyo origen sea la disponibilidad del dentista.</li>
     *   <li>Evalúa los turnos nuevamente en base a la nueva jornada laboral para detectar nuevos conflictos.</li>
     *   <li>Compara los conflictos nuevos con los existentes, determinando cuáles crear, reabrir o resolver.</li>
     *   <li>Persiste los cambios detectados en la base de datos.</li>
     *   <li>Mapea los conflictos finales a DTOs para la respuesta y envía una notificación por correo electrónico.</li>
     * </ol>
     *
     * @param idDentist Identificador único del dentista cuya disponibilidad fue modificada.
     * @param days Lista de objetos {@link WorkingDayDTO} que representan la nueva jornada laboral configurada.
     * @return Lista de {@link AppointmentConflictResponseDTO} con los conflictos actualizados luego de la reevaluación.
     */
    @Override
    @Transactional
    public List<AppointmentConflictResponseDTO> verifyConflictsByDentistAvailability(Long idDentist, List<WorkingDayDTO> days) {

        //Se obtienen los turnos futuros para el dentista.
        List<Appointment> appointments = appointmentRepository.findFutureAppointmentsReservedByDentist(idDentist, LocalDateTime.now(), AppointmentStatus.RESERVED);

        // Se obtiene los turnos conflictivos previos al cambio, y que el origen del conflicto fue la jornada laboral del dentista.
        List<AppointmentConflict> appointmentConflictsExisting = appointmentConflictService.getAllByDentistIdAndAvailabilityConflict(idDentist, OriginConflict.DENTIST_AVAILABILITIES);

        if (appointments.isEmpty() && appointmentConflictsExisting.isEmpty()) {
            return Collections.emptyList();
        }

        //Se limpian los conflictos previos con origen "Disponibilidad Laboral".
        updateResolvedConflicts(appointmentConflictsExisting);


        // Se reevalúan todos los turnos(en conflicto o no) para determinar si alguno está en conflicto por la nueva parametrización.
        List<AppointmentConflict> appointmentConflictsNew = verifyConflictByDentistAvailability(appointments,days);

        //Persistimos en base solo los nuevos conflictos
        List<AppointmentConflict> conflictSaved = appointmentConflictService.create(appointmentConflictsNew);


        //Mapeamos los conflictos persistidos a UN DTO para respuesta.
        List<AppointmentConflictResponseDTO> conflictsResponseDTO = conflictSaved.stream()
                .map(AppointmentConflictResponseDTO::build)
                .toList();


        //Notificación por mail.
        emailService.sendEmail(
                userService.getEmailByRole(List.of(Role.SECRETARY.toString(), Role.ADMINISTRATOR.toString())),
                messageSource.getMessage("conflictManagerService.dentistAvailability.notifyEmail.subject", new Object[]{authenticatedUserService.getAuthenticatedUser().getUsername()}, LocaleContextHolder.getLocale()),
                messageSource.getMessage("conflictManagerService.dentistAvailability.notifyEmail.body", new Object[]{authenticatedUserService.getAuthenticatedUser().getUsername()}, LocaleContextHolder.getLocale())
        );

        return conflictsResponseDTO;
    }


    /**
     * Evalúa si la creación de un nuevo bloqueo en el calendario de un dentista genera conflictos con turnos ya reservados.
     * <p>
     * El método analiza los turnos futuros del dentista y determina cuáles se superponen con el bloqueo propuesto,
     * considerando las fechas, horarios y tipo de recurrencia configurados. En caso de detectar conflictos,
     * los actualiza en la base de datos y los devuelve en la respuesta.
     * </p>
     *
     * <p>Flujo general:</p>
     * <ol>
     *   <li>Obtiene los turnos futuros del dentista afectado.</li>
     *   <li>Detecta los turnos en conflicto con el bloqueo según la configuración recibida.</li>
     *   <li>Si no hay conflictos, devuelve una lista vacía.</li>
     *   <li>Si hay conflictos, los persiste mediante el servicio correspondiente y los mapea a DTOs.</li>
     * </ol>
     *
     * @param dentistCalendarLockRequestCreateDTO Objeto que contiene los datos del bloqueo a registrar (fechas, días, horarios, etc.).
     * @param dentists Entidad {@link Dentist} afectada por el nuevo bloqueo.
     * @return Lista de {@link AppointmentConflictResponseDTO} con los turnos en conflicto detectados,
     *         o una lista vacía si no se encontraron conflictos.
     */

    public List<AppointmentConflictResponseDTO> verifyConflictsByDentistCalendarLock(DentistCalendarLockRequestCreateDTO dentistCalendarLockRequestCreateDTO, Dentist dentists) {
        //Obtiene turno por dentista.
        List<Appointment> appointments = appointmentRepository.findFutureAppointmentsReservedByDentist(dentists.getId(), LocalDateTime.now(), AppointmentStatus.RESERVED);

        // Identificar si hay turnos en conflictos.
        List<AppointmentConflict> appointmentConflicts = verifyConflictAppointmentsCalendarLock(appointments, dentistCalendarLockRequestCreateDTO,dentistCalendarLockRequestCreateDTO.getRecurrence());

        //Retorna en caso de lista vacía. Caso contrario, persiste conflictos y retorna
        if(appointmentConflicts.isEmpty()) {
            return Collections.emptyList();

        }

        //Persistimos en base solo los nuevos conflictos
        List<AppointmentConflict> conflictSaved = appointmentConflictService.create(appointmentConflicts);

        //Notificación por mail.
        emailService.sendEmail(
                userService.getEmailByRole(List.of(Role.SECRETARY.toString(), Role.ADMINISTRATOR.toString())),
                messageSource.getMessage("conflictManagerService.dentistLockCalendar.notifyEmail.subject", new Object[]{authenticatedUserService.getAuthenticatedUser().getUsername()}, LocaleContextHolder.getLocale()),
                messageSource.getMessage("conflictManagerService.dentistLockCalendar.notifyEmail.body", new Object[]{authenticatedUserService.getAuthenticatedUser().getUsername()}, LocaleContextHolder.getLocale())
        );


        //Mapea conflictos y devuelve
        return conflictSaved.stream()
                .map(AppointmentConflictResponseDTO::build)
                .toList();
    }








    /**
     * Detecta y genera conflictos de turnos que se encuentran fuera de la nueva jornada laboral de un dentista.
     * <p>
     * Este método compara cada turno futuro del dentista con la lista de {@link WorkingDayDTO} que define la nueva
     * disponibilidad laboral. Para cada turno que no se encuentra dentro de los días y horarios permitidos,
     * se genera un objeto {@link AppointmentConflict} indicando que está fuera de horario.
     * </p>
     *
     * @param appointments Lista de {@link Appointment} que representa los turnos futuros del dentista.
     * @param workingDays Lista de {@link WorkingDayDTO} que define la nueva jornada laboral a evaluar.
     * @return Lista de {@link AppointmentConflict} representando los turnos que no se ajustan a la nueva jornada laboral.
     */

    @Override
    public List<AppointmentConflict> verifyConflictByDentistAvailability(List<Appointment> appointments, List<WorkingDayDTO> workingDays) {

        List<AppointmentConflict> conflicts = new ArrayList<>();

        for (Appointment appointment : appointments) {
            boolean covered = false;
            Long idOriginConflict = null;
            OriginConflict originConflict = null;


            for (WorkingDayDTO workingDay : workingDays) {

                LocalDate startDate;
                LocalDate endDate;
                DayOfWeek day;

                // Jornada recurrente (semanal)
                if (workingDay.getSpecificDate() == null) {

                    startDate = workingDay.getEffectiveDate();

                    //Fecha fín (Hasta donde evalúa). Último turno encontrado.
                    endDate = appointments.stream()
                            .map(a -> a.getDate().toLocalDate())
                            .max(Comparator.naturalOrder())
                            .orElse(null);

                    day = workingDay.getDayName().toDayOfWeek();
                }

                // Jornada por fecha específica
                else {
                    startDate = workingDay.getSpecificDate();
                    endDate = workingDay.getEffectiveDate();
                    day = null;
                }

                // Si esta jornada cubre el turno, no hay conflicto
                if (CalendarUtils.isDateTimeWithinEvent(appointment.getDate(), startDate, endDate,day, workingDay.getStartTime(), workingDay.getEndTime(), workingDay.getRecurrence())) {
                    covered = true;
                    break;
                }

                // Guardamos último origen posible (si ninguna cubre)
                idOriginConflict = workingDay.getIdOriginConflict();
                originConflict = workingDay.getOriginConflict();

            }

            if (!covered) {

                AppointmentConflict ac = AppointmentConflict.build(
                        appointment,
                        idOriginConflict,
                        originConflict.name()

                );
                ac.setCreatedAt(LocalDateTime.now());
                ac.setCreatedBy(authenticatedUserService.getAuthenticatedUser());
                ac.setEnabled(true);

                conflicts.add(ac);
            }
        }
        return conflicts;
    }



    /**
     * Marca como resueltos los conflictos de turnos de un dentista recibida
     * @param appointmentConflicts Lista de {@link AppointmentConflict} que indica los turnos que deben marcarse como resueltos.
     */

    @Override
    public void updateResolvedConflicts(List<AppointmentConflict> appointmentConflicts) {

        List<Long> ids = appointmentConflicts.stream()
                .map(AppointmentConflict::getId)
                .toList();

        appointmentConflictService.resolvedAll(ids, LocalDateTime.now(), authenticatedUserService.getAuthenticatedUser());

    }



    /**
     * Resuelve los turnos en conflicto cuando un bloqueo de calendario se finaliza anticipadamente.
     * <p>
     * El método obtiene todos los conflictos asociados al bloqueo y, si existen, los marca como resueltos
     * mediante el servicio de gestión de conflictos.
     *
     * @param dentistCalendarLock Bloqueo de calendario que se está finalizando anticipadamente.
     */

    @Override
    public void resolvedAppointmentConflictByFinishLock(DentistCalendarLock dentistCalendarLock) {
        List<AppointmentConflict> appointmentConflicts = appointmentConflictService.getAllByDentistIdAndCalendarLockConflictId(dentistCalendarLock.getDentist().getId(), dentistCalendarLock.getId());

        if(!appointmentConflicts.isEmpty()){
            updateResolvedConflicts(appointmentConflicts);
        }
    }




    /**
     * Detecta y genera conflictos de turnos en función de un nuevo bloqueo aplicado al calendario de un dentista.
     * <p>
     * Este método compara cada turno futuro del dentista con los parámetros del bloqueo definido en
     * {@link DentistCalendarLockRequestCreateDTO} (fechas, días, horarios y recurrencia). Para cada turno que
     * se superpone con el bloqueo, se genera un {@link AppointmentConflict} indicando que está fuera de horario.
     * </p>
     *
     * @param appointments Lista de {@link Appointment} que representa los turnos futuros del dentista.
     * @param dentistCalendarLockRequestCreateDTO Objeto con los datos del bloqueo a evaluar (rango de fechas, días, horarios, etc.).

     * @param recurrence Tipo de recurrencia del bloqueo ({@link CalendarLockRecurrenceName}).
     * @return Lista de {@link AppointmentConflict} representando los turnos que entran en conflicto con el bloqueo.
     */

    private List<AppointmentConflict> verifyConflictAppointmentsCalendarLock(List<Appointment> appointments, DentistCalendarLockRequestCreateDTO dentistCalendarLockRequestCreateDTO, CalendarLockRecurrenceName recurrence) {
        List<AppointmentConflict> conflicts = new ArrayList<>();


        //Si la recurrencia es diaria, no hay Days en el DTO, agregamos provisoriamente para iterar.
        List<DayName> daysToEvaluate =
                dentistCalendarLockRequestCreateDTO.getRecurrence() == DAILY
                        ? DayName.listDayName()
                        : dentistCalendarLockRequestCreateDTO.getDays();


        for (Appointment appointment : appointments) {

            //flag para el for interno de days.
            boolean conflictDetected = false;

            //Si el bloqueo es PUNTUAL, no iteramos.
            if ((dentistCalendarLockRequestCreateDTO.getRecurrence() == null || dentistCalendarLockRequestCreateDTO.getRecurrence() == CalendarLockRecurrenceName.NONE)
                    && dentistCalendarLockRequestCreateDTO.getDays().isEmpty()
            ) {
                conflictDetected = CalendarUtils.isDateTimeWithinEvent(
                        appointment.getDate(),
                        dentistCalendarLockRequestCreateDTO.getStartDate(),
                        dentistCalendarLockRequestCreateDTO.getEndDate(),
                        null,
                        dentistCalendarLockRequestCreateDTO.getStartTime(),
                        dentistCalendarLockRequestCreateDTO.getEndTime(),
                        recurrence
                );
            }
            //Caso contrario, iteramos.
            else {
                for (DayName day : daysToEvaluate) {
                    if (CalendarUtils.isDateTimeWithinEvent(
                            appointment.getDate(),
                            dentistCalendarLockRequestCreateDTO.getStartDate(),
                            dentistCalendarLockRequestCreateDTO.getEndDate(),
                            day.toDayOfWeek(),
                            dentistCalendarLockRequestCreateDTO.getStartTime(),
                            dentistCalendarLockRequestCreateDTO.getEndTime(),
                            recurrence
                    )){
                        conflictDetected = true;
                        break; //cortamos el for para no seguir iterando por día

                    }
                }
            }

            if (conflictDetected) {
                AppointmentConflict ac = AppointmentConflict.build(
                        appointment,
                        dentistCalendarLockRequestCreateDTO.getIdOriginConflict(),
                        dentistCalendarLockRequestCreateDTO.getOriginConflict().name()
                );
                ac.setCreatedAt(LocalDateTime.now());
                ac.setCreatedBy(authenticatedUserService.getAuthenticatedUser());
                ac.setEnabled(true);

                conflicts.add(ac);
            }
        }
        return conflicts;
    }

}

