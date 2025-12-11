package com.odontologiaintegralfm.feature.appointment.core.service.impl;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.appointment.core.enums.AppointmentConflictMap;
import com.odontologiaintegralfm.feature.appointment.core.enums.AppointmentStatus;
import com.odontologiaintegralfm.feature.appointment.core.enums.CalendarLockRecurrenceName;
import com.odontologiaintegralfm.feature.appointment.catalogs.enums.DayName;
import com.odontologiaintegralfm.feature.appointment.core.dto.AppointmentConflictResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistCalendarLockRequestCreateDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.WorkingDayDTO;
import com.odontologiaintegralfm.feature.appointment.core.enums.OriginConflict;
import com.odontologiaintegralfm.feature.appointment.core.model.Appointment;
import com.odontologiaintegralfm.feature.appointment.core.model.AppointmentConflict;
import com.odontologiaintegralfm.feature.appointment.core.model.DentistAvailability;
import com.odontologiaintegralfm.feature.appointment.core.repository.IAppointmentRepository;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.IAppointmentConflictService;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.IConflictManagerService;
import com.odontologiaintegralfm.feature.authentication.enums.Role;
import com.odontologiaintegralfm.feature.dentist.core.model.Dentist;
import com.odontologiaintegralfm.feature.user.service.IUserService;
import com.odontologiaintegralfm.infrastructure.email.service.IEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio que centraliza la lógica para detectar y gestionar conflictos de turnos
 * cuando cambian la disponibilidad del odontólogo o se crean bloqueos en el calendario.
 * Contiene métodos para detectar conflictos nuevos, comparar con los ya existentes,
 * marcar conflictos como resueltos o reabrirlos y generar DTOs de respuesta.
 */
@Service
public class ConflictManagerService implements IConflictManagerService {

    @Autowired
    private AuthenticatedUserService authenticatedUserService;

    @Autowired
    private IAppointmentConflictService appointmentConflictService;

    @Autowired
    private IAppointmentRepository appointmentRepository;

    @Qualifier("messageSource")
    @Autowired
    private MessageSource messageSource;

    @Autowired
    private IUserService userService;

    @Autowired
    private IEmailService emailService;


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
    public List<AppointmentConflictResponseDTO> verifyConflictsByDentistAvailability(Long idDentist, List<WorkingDayDTO> days) {

        //Se obtienen los turnos futuros para el dentista.
        List<Appointment> appointments = appointmentRepository.findFutureAppointmentsReservedByDentist(idDentist, LocalDateTime.now(), AppointmentStatus.RESERVED);

        // Se obtiene los turnos conflictivos previos al cambio, y que el origen del conflicto fue la jornada laboral del dentista.
        List<AppointmentConflict> appointmentConflictsExisting = appointmentConflictService.getAllByDentistIdAndAvailabilityConflict(idDentist, OriginConflict.DENTIST_AVAILABILITIES);

        if (appointments.isEmpty() && appointmentConflictsExisting.isEmpty()) {
            return Collections.emptyList();
        }


        // Se reevalúan todos los turnos(en conflicto o no) para determinar si alguno está en conflicto por la nueva parametrización.
        List<AppointmentConflict> appointmentConflictsNew = conflictDetectorByDentistAvailability(appointments,days);


        //Comparamos turnos en conflictos por la nueva parametrización vs. Turnos en conflicto ya existente.
        Map<AppointmentConflictMap, List<AppointmentConflict>> conflictResult = compareConflictNewWithDataBase(appointmentConflictsNew, appointmentConflictsExisting);

        //Persistimos en base solo los nuevos conflictos
        appointmentConflictService.create(conflictResult.get(AppointmentConflictMap.CREATE));

        //Persistimos conflictos reabiertos.
        reopenConflicts(conflictResult.get(AppointmentConflictMap.REOPEN));

        //Persistimos como resueltos aquellos turnos en conflictos, que ya no lo están por la nueva parametrización.
        resolveOldConflicts(conflictResult.get(AppointmentConflictMap.RESOLVED));

        //Mapeamos los conflictos persistidos a UN DTO para respuesta.
        List<AppointmentConflictResponseDTO> conflictsResponseDTO = conflictResult.get(AppointmentConflictMap.FINAL).stream()
                .map(acs -> new AppointmentConflictResponseDTO(
                        acs.getAppointment().getId(),
                        acs.getAppointment().getDate(),
                        acs.getAppointment().getPatient().getPerson().getLastName() + "," + acs.getAppointment().getPatient().getPerson().getFirstName(),
                        acs.getIdOriginConflict(),
                        acs.getOriginConflict().getLabel()
                ))
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
        List<AppointmentConflict> appointmentConflicts = conflictDetectorByDentistCalendarLock(appointments, dentistCalendarLockRequestCreateDTO,dentists,dentistCalendarLockRequestCreateDTO.getRecurrence());

        //Retorna en caso de lista vacía. Caso contrario, persiste conflictos y retorna
        if(appointmentConflicts.isEmpty()) {
           return Collections.emptyList();

        }

       List <AppointmentConflict> appointmentConflict = appointmentConflictService.update(appointmentConflicts);

        //Notificación por mail.
        emailService.sendEmail(
                userService.getEmailByRole(List.of(Role.SECRETARY.toString(), Role.ADMINISTRATOR.toString())),
                messageSource.getMessage("conflictManagerService.dentistLockCalendar.notifyEmail.subject", new Object[]{authenticatedUserService.getAuthenticatedUser().getUsername()}, LocaleContextHolder.getLocale()),
                messageSource.getMessage("conflictManagerService.dentistLockCalendar.notifyEmail.body", new Object[]{authenticatedUserService.getAuthenticatedUser().getUsername()}, LocaleContextHolder.getLocale())
        );


        //Mapea conflictos y devuelve
        return appointmentConflict.stream()
                .map(ac -> new AppointmentConflictResponseDTO(
                        ac.getAppointment().getId(),
                        ac.getAppointment().getDate(),
                        ac.getAppointment().getPatient().getPerson().getLastName() + ", " + ac.getAppointment().getPatient().getPerson().getFirstName(),
                        ac.getIdOriginConflict(),
                        ac.getOriginConflict().getLabel()
                ))
                .toList();
    }



    /**
     * Compara los turnos en conflicto detectados con la nueva configuración del calendario frente a los conflictos ya existentes en base de datos.
     * <p>
     * Este método determina qué conflictos deben crearse, reabrirse o marcarse como resueltos en función de la comparación
     * entre la nueva lista de conflictos y los registros previos. También genera una lista final con los conflictos que
     * deben incluirse en la respuesta.
     * </p>
     *
     * <p>Reglas principales:</p>
     * <ul>
     *   <li>Si no existen nuevos conflictos, todos los conflictos previos se marcan como <b>RESOLVED</b>.</li>
     *   <li>Si un conflicto nuevo ya existía pero estaba resuelto, se marca como <b>REOPEN</b>.</li>
     *   <li>Si un conflicto nuevo no existía previamente, se marca como <b>CREATE</b>.</li>
     *   <li>Los conflictos de tipo CREATE y REOPEN se incluyen en la lista <b>FINAL</b> para el mapeo al DTO.</li>
     * </ul>
     *
     * @param appointmentConflictsNew Lista de {@link AppointmentConflict} generados con la nueva configuración o parametrización.
     * @param appointmentConflictsExisting Lista de {@link AppointmentConflict} obtenidos previamente desde la base de datos.
     * @return Un {@link Map} que agrupa los conflictos por tipo de acción mediante {@link AppointmentConflictMap},
     *         con las claves: CREATE, REOPEN, RESOLVED y FINAL.
     */
    private Map<AppointmentConflictMap, List<AppointmentConflict>> compareConflictNewWithDataBase(List<AppointmentConflict> appointmentConflictsNew, List<AppointmentConflict> appointmentConflictsExisting) {

        List<AppointmentConflict> appointmentConflictCreate = new ArrayList<>();
        List<AppointmentConflict> appointmentConflictUpdateResolved = new ArrayList<>();
        List<AppointmentConflict> appointmentConflictUpdateReopen = new ArrayList<>();
        List<AppointmentConflict> appointmentConflictFinal = new ArrayList<>();


        //Se indexan IDs del turno existentes para búsqueda directa.
        Map<Long, AppointmentConflict> existingById = new HashMap<>();
        for (AppointmentConflict existing : appointmentConflictsExisting) {
            existingById.put(existing.getAppointment().getId(), existing);

        }

        Map<Long,AppointmentConflict> newById = new HashMap<>();
        for(AppointmentConflict apNew : appointmentConflictsNew) {
            newById.put(apNew.getAppointment().getId(), apNew);
        }

        //Si no hay conflictos para la nueva parametrización, todos los existentes pasan a resueltos.
        if (appointmentConflictsNew.isEmpty()) {
            appointmentConflictUpdateResolved.addAll(appointmentConflictsExisting);
        } else {
            //Si hay conflictos por la nueva parametrización, comparo con los conflictos existentes para saber si es necesario crearlos o actualizarlos.
            for (AppointmentConflict appointmentConflictNew : appointmentConflictsNew) {
                AppointmentConflict match = existingById.get(appointmentConflictNew.getAppointment().getId());
                if (match != null && match.isResolved()) { // Si el conflicto existe y está resuelto, se reabre
                    appointmentConflictUpdateReopen.add(match);
                    appointmentConflictFinal.add(match);
                } else { // Si el conflicto no existe previamente, se agrega a la lista para persistir.
                    appointmentConflictCreate.add(appointmentConflictNew);
                    appointmentConflictFinal.add(appointmentConflictNew);
                }
            }


            //Proceso inverso. Comparo los conflictos en base vs. nuevos para Resolver los que ahora no están en conflicto.
            for(AppointmentConflict appointmentConflictExisting : appointmentConflictsExisting) {
                AppointmentConflict matchNew = newById.get(appointmentConflictExisting.getAppointment().getId());
                if(matchNew == null) {
                    appointmentConflictUpdateResolved.add(appointmentConflictExisting);
                }
            }
        }

        Map<AppointmentConflictMap, List<AppointmentConflict>> result = new HashMap<>();
        result.put(AppointmentConflictMap.CREATE, appointmentConflictCreate);
        result.put(AppointmentConflictMap.REOPEN, appointmentConflictUpdateReopen);
        result.put(AppointmentConflictMap.RESOLVED, appointmentConflictUpdateResolved);
        result.put(AppointmentConflictMap.FINAL, appointmentConflictFinal);

        return result;
    }



    /**
     * Reabre conflictos previamente resueltos que vuelven a aplicarse debido a cambios en la configuración o parametrización.
     * <p>
     * Este método actualiza los campos de auditoría de cada conflicto, marca el estado como no resuelto (resolved = false)
     * y asigna la razón del conflicto correspondiente antes de persistir los cambios.
     * </p>
     *
     * @param appointmentConflictUpdate Lista de {@link AppointmentConflict} que deben reabrirse y actualizarse.
     */

    private void reopenConflicts(List<AppointmentConflict> appointmentConflictUpdate) {

        appointmentConflictUpdate.forEach(appointmentConflict -> {
                    appointmentConflict.setResolved(false);
                    appointmentConflict.setUpdatedBy(authenticatedUserService.getAuthenticatedUser());
                    appointmentConflict.setUpdatedAt(LocalDateTime.now());

                }

        );

        appointmentConflictService.update(appointmentConflictUpdate);
    }


    /**
     * Marca como resueltos los conflictos que dejan de estar en ese estado tras cambios en la parametrización del calendario.
     * <p>
     * Actualiza los campos de auditoría de cada conflicto, establece el estado como resuelto (resolved = true)
     * y elimina el motivo del conflicto antes de persistir los cambios.
     * </p>
     *
     * @param resolved Lista de {@link AppointmentConflict} que deben actualizarse y marcarse como resueltos.
     */

    private void resolveOldConflicts(List<AppointmentConflict> resolved) {
        resolved.forEach(conflictExisting -> {
                    conflictExisting.setResolved(true);
                    conflictExisting.setUpdatedAt(LocalDateTime.now());
                    conflictExisting.setUpdatedBy(authenticatedUserService.getAuthenticatedUser());
                }
        );


        appointmentConflictService.update(resolved);

    }



    /**
     * Marca como resueltos los conflictos de turnos de un dentista basándose en una lista de conflictos calculada previamente.
     * <p>
     * Este método filtra los conflictos activos existentes del dentista para determinar cuáles corresponden
     * a la lista calculada, actualiza su estado a resuelto y registra los cambios de auditoría antes de persistirlos.
     * </p>
     *
     * @param dentistId Identificador único del dentista cuyos conflictos serán actualizados.
     * @param calculatedConflicts Lista de {@link AppointmentConflict} que indica los turnos que deben marcarse como resueltos.
     */

    @Override
    public void updateResolvedConflicts(Long dentistId, List<AppointmentConflict> calculatedConflicts) {
        //Buscamos los conflictos guardados en la base previamente.
        List<AppointmentConflict> appointmentConflicts = appointmentConflictService.getNotResolved(dentistId);


        //Tomamos solos los IDs de la lista recibida por parámetro.
        Set<Long> appointmentIds = calculatedConflicts.stream()
                .map(appointmentConflict -> appointmentConflict.getAppointment().getId())
                .collect(Collectors.toSet());


        List<AppointmentConflict> conflictsToResolve = appointmentConflicts.stream()
                .filter(appointmentConflict -> appointmentIds.contains(appointmentConflict.getAppointment().getId()))
                .toList();


        //Actualizamos los turnos en conflictos como resueltos.
        List<AppointmentConflict> conflictsResolved = new ArrayList<>();

        for(AppointmentConflict conflict : conflictsToResolve) {
            conflict.setResolved(true);
            conflict.setUpdatedAt(LocalDateTime.now());
            conflict.setUpdatedBy(authenticatedUserService.getAuthenticatedUser());
            conflictsResolved.add(conflict);
        }

        appointmentConflictService.update(conflictsResolved);

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
    public List<AppointmentConflict> conflictDetectorByDentistAvailability(List<Appointment> appointments, List<WorkingDayDTO> workingDays) {

        List<AppointmentConflict> conflicts = new ArrayList<>();

        LocalDate startDate;
        LocalDate endDate;
        DayOfWeek day;

        for (Appointment appointment : appointments) {
            Long idOriginConflict = null;
            OriginConflict originConflict = null;
            boolean covered = false;

            for (WorkingDayDTO workingDay : workingDays) {

                //evualar si especificDate no es nulo para setear fechas inicio y fin.
                if (workingDay.getSpecificDate() == null) {

                    startDate = workingDay.getEffectiveDate();

                    //Fecha fin (Hasta donde evalúa). último turno encontrado.
                    endDate = appointments.stream()
                            .map(a -> a.getDate().toLocalDate())
                            .max(Comparator.naturalOrder())
                            .orElse(null);

                    day = workingDay.getDayName().toDayOfWeek();
                } else {
                    startDate = workingDay.getSpecificDate();
                    endDate = workingDay.getEffectiveDate();
                    day = null;
                }

                if (hasAppointmentMatchWithEvent(appointment.getDate(), startDate, endDate,day, workingDay.getStartTime(), workingDay.getEndTime(), workingDay.getRecurrence())) {
                    covered = true;
                    idOriginConflict = workingDay.getIdOriginConflict();
                    originConflict = workingDay.getOriginConflict();
                }

            }

            if (covered) {
                conflicts.add(new AppointmentConflict(
                        null,
                        appointment,
                        idOriginConflict,
                        originConflict.name(),
                        LocalDateTime.now(),
                        authenticatedUserService.getAuthenticatedUser(),
                        true
                ));
            }
        }
        return conflicts;
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
     * @param dentist Entidad {@link Dentist} afectada por el bloqueo.
     * @param recurrence Tipo de recurrencia del bloqueo ({@link CalendarLockRecurrenceName}).
     * @return Lista de {@link AppointmentConflict} representando los turnos que entran en conflicto con el bloqueo.
     */

    private List<AppointmentConflict> conflictDetectorByDentistCalendarLock(List<Appointment> appointments, DentistCalendarLockRequestCreateDTO dentistCalendarLockRequestCreateDTO, Dentist dentist, CalendarLockRecurrenceName recurrence) {
        List<AppointmentConflict> conflicts = new ArrayList<>();


        for (Appointment appointment : appointments) {
            Long idOriginConflict = null;
            OriginConflict originConflict = null;
            boolean covered = false;

            //evualar si especificDate no es nulo para setear fechas inicio y fin.
            for(DayName day:dentistCalendarLockRequestCreateDTO.getDays()){
                if (hasAppointmentMatchWithEvent(appointment.getDate(), dentistCalendarLockRequestCreateDTO.getStartDate(), dentistCalendarLockRequestCreateDTO.getEndDate(), day.toDayOfWeek(), dentistCalendarLockRequestCreateDTO.getStartTime(), dentistCalendarLockRequestCreateDTO.getEndTime(), recurrence)) {
                    covered = true;
                } else {
                    idOriginConflict = dentistCalendarLockRequestCreateDTO.getIdOriginConflict();
                    originConflict = dentistCalendarLockRequestCreateDTO.getOriginConflict();
                }
            }

            if (!covered) {
                conflicts.add(new AppointmentConflict(
                        null,
                        appointment,
                        idOriginConflict,
                        originConflict.name(),
                        LocalDateTime.now(),
                        authenticatedUserService.getAuthenticatedUser(),
                        true
                ));
            }
        }

        return conflicts;

    }


    /**
     * Verifica si un turno específico coincide con un evento o bloqueo definido por rango de fechas, días válidos y horario.
     * <p>
     * La verificación considera la fecha y hora del turno, los días de la semana válidos para el evento,
     * el horario de inicio y fin, y la recurrencia del bloqueo (por ejemplo, NONE, DAILY, WEEKLY, etc.).
     * </p>
     *
     * @param appointmentDateTime Fecha y hora del turno a evaluar.
     * @param startDate Fecha de inicio del evento o bloqueo.
     * @param endDate Fecha de fin del evento o bloqueo.
     * @param day Lista de {@link DayOfWeek} que representan los días válidos del evento/bloqueo.
     * @param startTime Hora de inicio del evento/bloqueo.
     * @param endTime Hora de fin del evento/bloqueo.
     * @param recurrence Tipo de recurrencia del evento/bloqueo ({@link CalendarLockRecurrenceName}).
     * @return true si el turno cae dentro del evento o bloqueo según fecha, día, recurrencia y horario; false en caso contrario.
     */

    public boolean hasAppointmentMatchWithEvent(
            LocalDateTime appointmentDateTime,
            LocalDate startDate,
            LocalDate endDate,
            DayOfWeek day,
            LocalTime startTime,
            LocalTime endTime,
            CalendarLockRecurrenceName recurrence
    ) {
        LocalDate appointmentDate = appointmentDateTime.toLocalDate();
        LocalTime appointmentTime = appointmentDateTime.toLocalTime();


        // Verificar rango de fechas para jornada específicas.
        if (day == null && recurrence == null) {
            if (appointmentDate.isBefore(startDate) || appointmentDate.isAfter(endDate)) {
                return false;
            }
        }


        // Verificar día + recurrencia combinados
        if(day != null){
            DayOfWeek appointmentDay = appointmentDate.getDayOfWeek();

            boolean isValidDayAndRecurrence =
                    day.equals(appointmentDay)
                            && validateRecurrence(recurrence, startDate, appointmentDate);

            if (!isValidDayAndRecurrence) {
                return false;
            }
        }

        // Verificar rango horario
        return !appointmentTime.isBefore(startTime) && !appointmentTime.isAfter(endTime);
    }






    /**
     * Verifica si todas las fechas efectivas de un bloqueo están completamente cubiertas
     * por alguna disponibilidad del dentista.
     *
     * <p>
     * Para cada fecha generada según la recurrencia del bloqueo, el método evalúa si existe al menos
     * una disponibilidad del dentista que coincida en la recurrencia (día y/o fecha según corresponda)
     * y además cubra totalmente el rango horario del bloqueo.
     * </p>
     *
     * <p>Flujo de validación:</p>
     * <ol>
     *   <li>Genera todas las fechas efectivas del bloqueo según su recurrencia.</li>
     *   <li>Para cada fecha del bloqueo:
     *     <ol>
     *       <li>Busca al menos una disponibilidad que coincida en recurrencia.</li>
     *       <li>Si coincide en fecha, valida que la disponibilidad cubra completamente el rango horario.</li>
     *     </ol>
     *   </li>
     *   <li>Si alguna fecha del bloqueo no encuentra ninguna disponibilidad que coincida y cubra el horario, retorna false.</li>
     *   <li>Si todas las fechas están cubiertas, retorna true.</li>
     * </ol>
     *
     * @param availabilities Lista de disponibilidades del dentista.
     * @param startDateBlock Fecha de inicio del bloqueo.
     * @param endDateBlock Fecha de fin del bloqueo.
     * @param startTimeBlock Hora de inicio del bloqueo.
     * @param endTimeBlock Hora de fin del bloqueo.
     * @param recurrenceBlock Tipo de recurrencia del bloqueo ({@link CalendarLockRecurrenceName}).
     *
     * @return true si todas las fechas efectivas del bloqueo están cubiertas; false si alguna fecha no lo está.
     */

    public boolean hasBlockMatchWithAvailability(
            List<DentistAvailability> availabilities,
            LocalDate startDateBlock,
            LocalDate endDateBlock,
            LocalTime startTimeBlock,
            LocalTime endTimeBlock,
            CalendarLockRecurrenceName recurrenceBlock) {

        boolean conflict = true;


        // 1. Generar todas las fechas del bloqueo según su recurrencia
        Set<LocalDate> blockDates = generateEffectiveDates(startDateBlock, endDateBlock, recurrenceBlock);

        for (LocalDate blockDate : blockDates) {
            for(DentistAvailability availability: availabilities) {
                boolean match = validateRecurrence(recurrenceBlock, availability.getEffectiveDate(), blockDate);
                if (match) {
                    // Verificar rango horario
                    if (availability.getStartTime().isAfter(startTimeBlock) // empieza después
                            || availability.getEndTime().isBefore(endTimeBlock)) { // termina antes
                        return false;
                    }
                    conflict = false;
                }
            }
            if(conflict){
                return false;
            }
        }
        return true;
    }






    /**
     * Genera una lista de fechas efectivas para un bloqueo de calendario según la recurrencia especificada.
     * <p>
     * Dependiendo del tipo de recurrencia, este método calcula todas las fechas dentro del rango
     * entre {@code startDate} y {@code endDate} que corresponden al patrón definido:
     * </p>
     *
     * <ul>
     *     <li><b>NONE:</b> Solo la fecha de inicio.</li>
     *     <li><b>DAILY:</b> Todos los días consecutivos entre startDate y endDate (inclusive).</li>
     *     <li><b>WEEKLY:</b> Todos los días que coinciden semanalmente con el día de startDate.</li>
     *     <li><b>BIWEEKLY:</b> Todos los días que coinciden cada dos semanas con el día de startDate.</li>
     *     <li><b>MONTHLY:</b> Todos los días del mes que coinciden con el día del mes de startDate.</li>
     *     <li><b>YEARLY:</b> Todos los días del año que coinciden con la fecha de startDate.</li>
     * </ul>
     *
     * <p>Este método solo genera las fechas según la recurrencia y no valida
     * si coinciden con los días laborales o disponibilidades del dentista.</p>
     *
     * @param startDate Fecha de inicio del bloqueo. No puede ser {@code null}.
     * @param endDate Fecha de fin del bloqueo. No puede ser {@code null} para recurrencias que requieren rango.
     * @param recurrence Tipo de recurrencia del bloqueo ({@link CalendarLockRecurrenceName}).
     * @return Lista de {@link LocalDate} que contiene todas las fechas efectivas generadas según la recurrencia.
     */

    public Set<LocalDate> generateEffectiveDates(LocalDate startDate, LocalDate endDate, CalendarLockRecurrenceName recurrence) {
        Set<LocalDate> dates = new HashSet<>();

        switch (recurrence) {
            case NONE:
                dates.add(startDate);
                break;

            case DAILY:
                for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
                    dates.add(d);
                }
                break;

            case WEEKLY:
                for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusWeeks(1)) {
                    dates.add(d);
                }
                break;

            case BIWEEKLY:
                for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusWeeks(2)) {
                    dates.add(d);
                }
                break;

            case MONTHLY:
                for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusMonths(1)) {
                    dates.add(d);
                }
                break;

            case YEARLY:
                for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusYears(1)) {
                    dates.add(d);
                }
                break;
        }

        return dates;
    }


    /**
     * Valída si una fecha específica coincide con un patrón de recurrencia a partir de una fecha de inicio.
     * <p>
     * Este método determina si {@code currentDate} cumple con la recurrencia definida en {@code recurrence}
     * tomando como referencia {@code startDate}. Se utiliza para validar bloqueos y disponibilidades recurrentes.
     * </p>
     *
     * <ul>
     *     <li><b>DAILY:</b> Siempre retorna true, cualquier fecha es válida.</li>
     *     <li><b>WEEKLY:</b> La fecha coincide si tiene el mismo día de la semana que startDate.</li>
     *     <li><b>BIWEEKLY:</b> La fecha coincide si tiene el mismo día de la semana y transcurrieron 0, 2, 4... semanas desde startDate.</li>
     *     <li><b>MONTHLY:</b> La fecha coincide si está en la misma semana del mes y día de la semana que startDate.</li>
     *     <li><b>YEARLY:</b> La fecha coincide si tiene el mismo mes y día que startDate.</li>
     *     <li><b>NONE:</b> Solo coincide si la fecha es exactamente igual a startDate.</li>
     * </ul>
     *
     * @param recurrence Tipo de recurrencia ({@link CalendarLockRecurrenceName}).
     * @param startDate Fecha de inicio real(no el día posterior) que sirve como referencia para la recurrencia.
     * @param currentDate Fecha que se desea validar contra la recurrencia.
     * @return true si currentDate cumple con la recurrencia definida respecto a startDate; false en caso contrario.
     */

    public boolean validateRecurrence(CalendarLockRecurrenceName recurrence, LocalDate startDate, LocalDate currentDate) {

        switch (recurrence) {
            case DAILY:
                return true;
            case WEEKLY:
                return startDate.getDayOfWeek() == currentDate.getDayOfWeek();
            case BIWEEKLY:
                long weeks = ChronoUnit.WEEKS.between(startDate, currentDate);
                return startDate.getDayOfWeek() == currentDate.getDayOfWeek() && weeks % 2 == 0;
            case MONTHLY:
                int startWeekOfMonth = (startDate.getDayOfMonth() - 1) / 7; // dia 7 de la semana dividido 7 = 1, incorrecto. Por eso se resta 1
                int currentWeekOfMonth = (currentDate.getDayOfMonth() - 1) / 7;
                return startDate.getDayOfWeek() == currentDate.getDayOfWeek()
                        && startWeekOfMonth == currentWeekOfMonth;
            case YEARLY:
                return startDate.getMonth() == currentDate.getMonth()
                        && startDate.getDayOfMonth() == currentDate.getDayOfMonth();
            case NONE:
                return startDate.equals(currentDate);
            default:
                return false;
        }
    }





    /**
     * Obtiene la primera fecha, a partir de una fecha inicial dada, que coincida con un día específico
     * de la semana dentro de los próximos 7 días.
     *
     * <p>El método avanza día por día desde {@code startDate} hasta un máximo de 7 días
     * buscando el primer {@link DayOfWeek} que coincida con el día solicitado en {@code daysBlock}.
     * Si encuentra una coincidencia, retorna esa fecha. Si no la encuentra dentro del rango
     * de 7 días, retorna la misma {@code startDate}.</p>
     *
     * <p>Este método se usa para calcular la fecha efectiva en la que debe comenzar
     * un evento recurrente o un bloqueo en el calendario, alineado al día de la semana
     * apropiado.</p>
     *
     * @param startDate Fecha a partir de la cual se inicia la búsqueda.
     * @param daysBlock Día de la semana que se quiere encontrar.
     * @return La primera fecha igual a {@code daysBlock} dentro de los próximos 7 días,
     *         o {@code startDate} si no se encuentra coincidencia.
     */
    public LocalDate findFirstMatchingDate(LocalDate startDate, DayOfWeek daysBlock) {

        // Buscar el primer día (en los próximos 7) que coincida con alguno de los días activos
        for (int i = 0; i < 7; i++) {
            LocalDate candidate = startDate.plusDays(i);
            if (daysBlock.equals(candidate.getDayOfWeek())) {
                return candidate;
            }
        }
        return startDate;
    }



    /**
     * Obtiene la última fecha, hacia atrás desde una fecha final dada, que coincida con un día específico
     * de la semana dentro de los últimos 7 días.
     *
     * <p>El método retrocede día por día desde {@code endDate} hasta un máximo de 7 días
     * buscando el último {@link DayOfWeek} que coincida con el día solicitado en {@code daysBlock}.
     * Si encuentra una coincidencia, retorna esa fecha. Si no encuentra ninguna en el rango,
     * retorna la misma {@code endDate}.</p>
     *
     * <p>Este método se utiliza para calcular la fecha efectiva final de un evento o bloqueo recurrente,
     * alineando la fecha de fin al día de la semana correspondiente.</p>
     *
     * @param endDate Fecha desde la cual se inicia la búsqueda hacia atrás.
     * @param daysBlock Día de la semana que se quiere encontrar.
     * @return La última fecha igual a {@code daysBlock} dentro de los últimos 7 días,
     *         o {@code endDate} si no se encuentra coincidencia.
     */
    public LocalDate findLastMatchingDate(LocalDate endDate, DayOfWeek daysBlock) {

        // Buscar el último día (en los próximos 7) que coincida con alguno de los días activos
        for (int i = 0; i < 7; i++) {
            LocalDate candidate = endDate.minusDays(i);
            if (daysBlock.equals(candidate.getDayOfWeek())) {
                return candidate;
            }
        }
        return endDate;
    }
}

