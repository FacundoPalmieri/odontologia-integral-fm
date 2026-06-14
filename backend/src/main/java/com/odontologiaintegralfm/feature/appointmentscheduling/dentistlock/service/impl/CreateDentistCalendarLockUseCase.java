package com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.service.impl;


import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.dto.AppointmentConflictResponseDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.model.Appointment;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.model.AppointmentConflict;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.service.AppointmentConflictService;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.service.AppointmentQueryService;
import com.odontologiaintegralfm.feature.appointmentscheduling.calendar.enums.CalendarLockRecurrenceName;
import com.odontologiaintegralfm.feature.appointmentscheduling.calendar.enums.OriginConflict;
import com.odontologiaintegralfm.feature.appointmentscheduling.calendar.util.CalendarUtils;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.service.ICreateDentistCalendarLockUseCase;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.service.IDentistCalendarLockService;
import com.odontologiaintegralfm.feature.appointmentscheduling.shared.ConflictManagerContextInternalDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistavailability.model.DentistAvailability;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistavailability.service.IDentistAvailabilityService;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistholiday.service.IDentistHolidayService;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.dto.DentistCalendarLockContextInternalDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.dto.DentistCalendarLockRequestCreateDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.dto.DentistCalendarLockResponseDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.model.DentistCalendarLock;
import com.odontologiaintegralfm.feature.appointmentscheduling.holiday.model.Holiday;
import com.odontologiaintegralfm.feature.appointmentscheduling.holiday.service.IHolidayService;
import com.odontologiaintegralfm.feature.appointmentscheduling.locktype.model.CalendarLockType;
import com.odontologiaintegralfm.feature.appointmentscheduling.locktype.service.ICalendarLockTypeService;
import com.odontologiaintegralfm.feature.appointmentscheduling.shared.DayName;
import com.odontologiaintegralfm.feature.authentication.enums.Role;
import com.odontologiaintegralfm.feature.dentist.core.model.Dentist;
import com.odontologiaintegralfm.feature.dentist.core.service.interfaces.IDentistService;
import com.odontologiaintegralfm.feature.user.service.IUserService;
import com.odontologiaintegralfm.infrastructure.email.service.IEmailService;
import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.enums.LogType;
import com.odontologiaintegralfm.shared.exception.BadRequestException;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static com.odontologiaintegralfm.feature.appointmentscheduling.calendar.enums.CalendarLockRecurrenceName.DAILY;

@Service
public class CreateDentistCalendarLockUseCase implements ICreateDentistCalendarLockUseCase {


    private final IDentistCalendarLockService dentistCalendarLockService;
    private final IDentistService dentistService;
    private final ICalendarLockTypeService calendarLockTypeService;
    private final IHolidayService holidayService;
    private final IDentistHolidayService dentistHolidayService;
    private final IDentistAvailabilityService dentistAvailabilityService;
    private final AuthenticatedUserService authenticatedUserService;
    private final MessageSource messageSource;
    private final AppointmentConflictService appointmentConflictService;
    private final IEmailService emailService;
    private final IUserService userService;
    private final AppointmentQueryService appointmentQueryService;

    public CreateDentistCalendarLockUseCase(
            IDentistCalendarLockService dentistCalendarLockService,
            IDentistService dentistService,
            ICalendarLockTypeService calendarLockTypeService,
            IHolidayService holidayService,
            IDentistHolidayService dentistHolidayService,
            IDentistAvailabilityService dentistAvailabilityService,
            AuthenticatedUserService authenticatedUserService,
            MessageSource messageSource,
            AppointmentConflictService appointmentConflictService,
            IEmailService emailService,
            IUserService userService,
            AppointmentQueryService appointmentQueryService

    ){
        this.dentistCalendarLockService = dentistCalendarLockService;
        this.dentistService = dentistService;
        this.calendarLockTypeService = calendarLockTypeService;
        this.holidayService = holidayService;
        this.dentistHolidayService = dentistHolidayService;
        this.dentistAvailabilityService = dentistAvailabilityService;
        this.authenticatedUserService = authenticatedUserService;
        this.messageSource = messageSource;
        this.appointmentConflictService = appointmentConflictService;
        this.emailService = emailService;
        this.userService = userService;
        this.appointmentQueryService = appointmentQueryService;
    }




    /**
     * Crea un bloqueo de calendario para un dentista, evaluando posibles conflictos con turnos
     * existentes y registrando la recurrencia si aplica. El método combina validaciones, preparación
     * del contexto, persistencia del bloqueo y verificación de conflictos.
     *
     * @param idPerson                            ID de la persona correspondiente al dentista.
     * @param dentistCalendarLockRequestCreateDTO DTO que contiene los datos del bloqueo (fechas, horarios, recurrencia, días, etc.).
     * @return Response<DentistCalendarLockResponseDTO> con la información del bloqueo creado y los posibles turnos en conflicto.
     */
    @Transactional
    @LogAction(
            value = "dentistCalendarLockCreateUseCase.logAction.execute",
            args  = {
                    "#idPerson",
                    "#result.data.startDate",
                    "#result.data.endDate",
                    "#result.data.mode",
                    "#result.data.recurrence",
                    "#result.data.appointmentConflict != null ? #result.data.appointmentConflict.size() : 0"
            },
            type  = LogType.SYSTEM,
            level = LogLevel.INFO
    )
    @Override
    public Response<DentistCalendarLockResponseDTO> execute(Long idPerson, DentistCalendarLockRequestCreateDTO dentistCalendarLockRequestCreateDTO) {


        // Prepara contexto (validaciones + construcción de objeto en memoria)
        DentistCalendarLockContextInternalDTO dentistCalendarLock = prepareContextDentistCalendarLock(idPerson, dentistCalendarLockRequestCreateDTO);

        //Persiste y crea el detalle de bloqueo si corresponde.
        DentistCalendarLock dentistCalendarLockSaved = dentistCalendarLockService.create(dentistCalendarLock, dentistCalendarLockRequestCreateDTO);


        //Seteo recurrencia, id y origen de posible conflicto en el DTO.
        dentistCalendarLockRequestCreateDTO.setIdOriginConflict(dentistCalendarLockSaved.getId());
        dentistCalendarLockRequestCreateDTO.setOriginConflict(OriginConflict.DENTIST_CALENDAR_LOCK);

        //Validar si existen turnos conflictivos.
        List<AppointmentConflictResponseDTO> appointmentConflicts = verifyConflictsByDentistCalendarLock(dentistCalendarLockRequestCreateDTO, dentistCalendarLock.dentistCalendarLock().getDentist());


        return new Response<>(
                true,
                (appointmentConflicts.isEmpty())
                        ? messageSource.getMessage("dentistCalendarLockCreateUseCase.execute.ok.user", null, LocaleContextHolder.getLocale())
                        : messageSource.getMessage("dentistCalendarLockCreateUseCase.execute.okWithConflict.user", null, LocaleContextHolder.getLocale()),
                DentistCalendarLockResponseDTO.build(dentistCalendarLockSaved, appointmentConflicts)
        );
    }






    /**
     * Método para simular un bloqueo de calendario, lo que permite detectar posibles conflictos con turnos.
     *
     * @param idPerson                            : Id Dentista.
     * @param dentistCalendarLockRequestCreateDTO : Datos del evento.
     */
    @Override
    public Response<DentistCalendarLockResponseDTO> executePreview(Long idPerson, DentistCalendarLockRequestCreateDTO dentistCalendarLockRequestCreateDTO) {

        // Prepara contexto (validaciones + construcción de objeto en memoria)
        DentistCalendarLockContextInternalDTO dentistCalendarLock = prepareContextDentistCalendarLock(idPerson,dentistCalendarLockRequestCreateDTO);


        //Seteo recurrencia, id y origen de posible conflicto en el DTO.
        dentistCalendarLockRequestCreateDTO.setIdOriginConflict(null); //No se puede obtener el ID del nuevo lock porque no se persiste.
        dentistCalendarLockRequestCreateDTO.setOriginConflict(OriginConflict.DENTIST_CALENDAR_LOCK);

        //Validar si existen turnos conflictivos.
        List<AppointmentConflictResponseDTO> appointmentConflicts = PreviewVerifyConflictsByDentistCalendarLock(dentistCalendarLockRequestCreateDTO,dentistCalendarLock.dentistCalendarLock().getDentist());

        return new Response<>(
                true,
                (appointmentConflicts.isEmpty())
                        ? messageSource.getMessage("dentistCalendarLockService.preview.ok.user",null, LocaleContextHolder.getLocale())
                        : messageSource.getMessage("dentistLockCalendarService.preview.okWithConflict.user", null, LocaleContextHolder.getLocale()),
                DentistCalendarLockResponseDTO.build(dentistCalendarLock.dentistCalendarLock(),appointmentConflicts)
        );
    }




    /**
     * Prepara el contexto necesario para la creación o preview de un bloqueo de calendario de un dentista.
     * <p>
     * Este método realiza las siguientes tareas:
     * <ul>
     *     <li>Valida que el dentista exista.</li>
     *     <li>Valida que la fecha de inicio no sea anterior al día actual.</li>
     *     <li>Valida que la fecha de fin no sea anterior a la fecha de inicio.</li>
     *     <li>Valida que el bloqueo no supere un año de duración.</li>
     *     <li>Valida el tipo de bloqueo según el evento y datos enviados.</li>
     *     <li>Valida y serializa la recurrencia para bloqueos diarios.</li>
     *     <li>Valida el rango de fechas para recurrencias semanales, quincenales, mensuales o anuales.</li>
     *     <li>Valida la cobertura de la jornada laboral para recurrencias distintas a diaria.</li>
     *     <li>Verifica que no exista un bloqueo previo con la misma combinación de fechas, recurrencia y días.</li>
     *     <li>Construye un objeto {@link DentistCalendarLock} con la información validada y completa los campos de auditoría.</li>
     * </ul>
     *
     * @param idPerson                          el ID del dentista para quien se crea el bloqueo
     * @param dentistCalendarLockRequestCreateDTO el DTO con los datos del bloqueo a crear o simular
     * @return un {@link DentistCalendarLockContextInternalDTO} que contiene el DTO de entrada actualizado
     *         y el objeto {@link DentistCalendarLock} construido en memoria listo para persistencia o preview
     *
     * @throws NotFoundException    si el dentista no existe en la base de datos
     * @throws ConflictException    si alguna de las validaciones de fechas, recurrencia o duplicados falla
     */

    private DentistCalendarLockContextInternalDTO prepareContextDentistCalendarLock(Long idPerson, DentistCalendarLockRequestCreateDTO dentistCalendarLockRequestCreateDTO){

        //Obtiene el dentista
        Dentist dentists = dentistService.getById(idPerson)
                .orElseThrow(()-> new NotFoundException("exception.dentistNotFound.user", null,"exception.dentistNotFound.log",new Object[]{idPerson,"DentistHolidayService","create"},LogLevel.ERROR));


        //Valída que el inicio no sea anterior al día actual.
        if(dentistCalendarLockRequestCreateDTO.getStartDate().isBefore(LocalDate.now())){
            throw new ConflictException("exception.dentistLockCalendarService.validateStarDateBeforeNow.user",null,"exception.dentistLockCalendarService.validateStarDateBeforeNow.log",new Object[]{dentists.getId(),dentistCalendarLockRequestCreateDTO.getStartDate(),"Dentist Calendar Lock Service","create" },LogLevel.ERROR);
        }

        //Valída que la fecha de fin no sea anterior a la fecha de inicio.
        if(dentistCalendarLockRequestCreateDTO.getEndDate().isBefore(dentistCalendarLockRequestCreateDTO.getStartDate())){
            throw new ConflictException("exception.dentistLockCalendarService.validateEndDateBeforeStartDate.user",null,"exception.dentistLockCalendarService.validateEndDateBeforeStartDate.log",new Object[]{dentists.getId(),dentistCalendarLockRequestCreateDTO.getStartDate(),dentistCalendarLockRequestCreateDTO.getEndDate(),"Dentist Calendar Lock Service","create"},LogLevel.ERROR);
        }

        //Valida que el bloqueo no supere un año.
        if(dentistCalendarLockRequestCreateDTO.getEndDate().isAfter(dentistCalendarLockRequestCreateDTO.getStartDate().plusYears(1))){
            throw new ConflictException("exception.dentistLockCalendarService.validateMaximumOneYear.user",null,"exception.dentistLockCalendarService.validateMaximumOneYear.log",new Object[]{dentists.getId(),dentistCalendarLockRequestCreateDTO.getStartDate(),dentistCalendarLockRequestCreateDTO.getEndDate(),"Dentist Calendar Lock Service","create"},LogLevel.ERROR);
        }

        //Validar Evento y tipos de datos de entrada para cada caso.
        CalendarLockType calendarLockType = calendarLockTypeService.findById(dentistCalendarLockRequestCreateDTO.getIdLockType());
        if (!calendarLockType.getModes().contains(dentistCalendarLockRequestCreateDTO.getMode())) {
            throw new BadRequestException("exception.dentistLockCalendarService.validateMode.user",null,"exception.dentistLockCalendarService.validateMode.log",new Object[]{dentists.getId(),dentistCalendarLockRequestCreateDTO.getMode(),"Dentist Calendar Lock Service","create"},LogLevel.ERROR);
        }
        dentistCalendarLockService.validateByMode(dentistCalendarLockRequestCreateDTO);



        //Validación de recurrencia para casos semanales, quincenales, mensuales y anuales. Que la ventana de fechas de bloqueos al menos cubra la recurrencia enviada.
        if(dentistCalendarLockRequestCreateDTO.getRecurrence() != null){
            dentistCalendarLockService.validateRecurrenceRange(dentistCalendarLockRequestCreateDTO.getRecurrence(),dentistCalendarLockRequestCreateDTO.getStartDate(),dentistCalendarLockRequestCreateDTO.getEndDate());
        }


        //Valída jornada laboral para casos de recurrencia NO diaria. Para los casos Daily(vacaciones) no se valida la jornada.
        if (dentistCalendarLockRequestCreateDTO.getRecurrence() != CalendarLockRecurrenceName.DAILY) {
            validateCoverage(
                    idPerson,
                    dentistCalendarLockService.generateEffectiveDates(
                            dentistCalendarLockRequestCreateDTO.getStartDate(),
                            dentistCalendarLockRequestCreateDTO.getStartDate(),
                            dentistCalendarLockRequestCreateDTO.getEndDate(),
                            dentistCalendarLockRequestCreateDTO.getRecurrence(),
                            dentistCalendarLockRequestCreateDTO.getDays()
                    ),
                    dentistCalendarLockRequestCreateDTO.getStartTime(),
                    dentistCalendarLockRequestCreateDTO.getEndTime(),
                    dentistCalendarLockRequestCreateDTO.isFullDay()
            );
        }


        //Valída sí existe relación feriado-dentista. Si es así, deshabilita la relación ya qué prevalece el bloqueo.
        //Busca entre fecha inicio y fin todos los feriados. Verificar uno x uno si hay relación de trabajo y deshabilitarlos.
        Map<LocalDate, Holiday> holidayList = holidayService.getByDateRange(dentistCalendarLockRequestCreateDTO.getStartDate(), dentistCalendarLockRequestCreateDTO.getEndDate());
        holidayList.forEach((date, holiday) -> {
            dentistHolidayService.VerifyAndDisabled(holiday.getId(), idPerson);

        });



        //Valída que no exista otro bloqueo que sea misma Fecha inicio - fin - recurrencia - dias.
        dentistCalendarLockService.verifyLockMatchWithLock(idPerson, dentistCalendarLockRequestCreateDTO);



        //Crea el bloqueo.
        DentistCalendarLock dentistCalendarLock = DentistCalendarLock.build(
                dentists,
                dentistCalendarLockRequestCreateDTO.getStartDate(),
                dentistCalendarLockRequestCreateDTO.getEndDate(),
                dentistCalendarLockRequestCreateDTO.getStartTime(),
                dentistCalendarLockRequestCreateDTO.getEndTime(),
                dentistCalendarLockRequestCreateDTO.isFullDay(),
                calendarLockType,
                dentistCalendarLockRequestCreateDTO.getMode(),
                dentistCalendarLockRequestCreateDTO.getRecurrence(),
                dentistCalendarLockRequestCreateDTO.getObservation()
        );

        //Setea campos de auditoria.
        dentistCalendarLock.setCreatedAt(LocalDateTime.now());
        dentistCalendarLock.setCreatedBy(authenticatedUserService.getAuthenticatedUser());
        dentistCalendarLock.setEnabled(true);


        return DentistCalendarLockContextInternalDTO.build(dentistCalendarLockRequestCreateDTO, dentistCalendarLock);

    }




    /**
     * Valída que un bloqueo de calendario propuesto coincida y este totalmente contenido dentro de la jornada laboral del dentista.
     *
     * <p>Dependiendo del tipo de recurrencia del bloqueo:</p>
     * <ul>
     *     <li><b>DAILY:</b> El bloqueo debe cubrir todos los días laborales del dentista dentro del rango de fechas.</li>
     *     <li><b>WEEKLY / MONTHLY / YEARLY / NONE:</b> El bloqueo debe coincidir con al menos un día laboral del dentista.</li>
     * </ul>
     *
     * <p>El método obtiene las disponibilidades del dentista y verifica si las fechas y horarios del bloqueo
     * están completamente cubiertos según la recurrencia.
     *
     * Impide que un bloqueo quede por partes fuera de la jornada laboral.
     *
     * </p>
     *
     * @param idDentist Id del dentista cuyo calendario se valida.
     * @param blocksDate Fechas de bloqueos
     * @param startTimeBlock Hora de inicio del bloqueo.
     * @param endTimeBlock Hora de fin del bloqueo.
     *
     * @throws BadRequestException Si el bloqueo no cumple con la cobertura requerida según la recurrencia y jornada del dentista.
     */
    private void validateCoverage(Long idDentist, List<LocalDate> blocksDate, LocalTime startTimeBlock, LocalTime endTimeBlock, boolean fullDay) {

        List<DentistAvailability> availabilities = dentistAvailabilityService.findById(idDentist);

        for (LocalDate blockDate : blocksDate) {

            boolean covered = false;

            for (DentistAvailability availability : availabilities) {

                // CASO 1: disponibilidad puntual (fecha específica)
                if (availability.getSpecificDate() != null) {

                    if (!availability.getEffectiveDate().equals(blockDate)) {
                        continue;
                    }

                    //Evalúa horarios SOLO si el flag de fullDay es false
                    if(fullDay){
                        covered = true;
                        break;
                    }

                    if (availability.getStartTime().isAfter(startTimeBlock) && availability.getEndTime().isBefore(endTimeBlock)) {
                        continue;
                    }

                    //Valída sí existe relación feriado-dentista. Si es así, deshabilita la relación ya qué prevalece el bloqueo.
                    Optional<Holiday> holiday = holidayService.getByDate(blockDate);
                    if(holiday.isPresent()){
                        dentistHolidayService.VerifyAndDisabled(holiday.get().getId(), idDentist);
                    }



                    covered = true;
                    break;
                }



                // CASO 2: Días sin recurrencia
                if (availability.getRecurrence() == null) {

                    if (availability.getKeyName() != DayName.fromDayOfWeek(blockDate.getDayOfWeek())) {
                        continue;
                    }


                    //Evalúa horarios SOLO si el flag de fullDay es false
                    if(fullDay){
                        covered = true;
                        break;
                    }
                    if (availability.getStartTime().isAfter(startTimeBlock) && availability.getEndTime().isBefore(endTimeBlock)) {
                        continue;
                    }

                    //Valída sí existe relación feriado-dentista. Si es así, deshabilita la relación ya qué prevalece el bloqueo.
                    Optional <Holiday> holiday = holidayService.getByDate(blockDate);
                    if(holiday.isPresent()){
                        dentistHolidayService.VerifyAndDisabled(holiday.get().getId(), idDentist);
                    }

                    covered = true;
                    break;
                }


                // CASO 3: Días + recurrencia
                if (availability.getKeyName() != DayName.fromDayOfWeek(blockDate.getDayOfWeek())) {
                    continue;
                }

                // 2. Match por recurrencia
                if (!availability.getRecurrence().matches(availability.getEffectiveDate(), blockDate)){
                    continue;
                }

                // 3. Rango horario

                //Evalúa horarios SOLO si el flag de fullDay es false
                if(fullDay){
                    covered = true;
                    break;
                }

                if (availability.getStartTime().isAfter(startTimeBlock) || availability.getEndTime().isBefore(endTimeBlock)) {
                    continue;
                }

                //Valída sí existe relación feriado-dentista. Si es así, deshabilita la relación ya qué prevalece el bloqueo.
                Optional <Holiday> holiday = holidayService.getByDate(blockDate);
                if(holiday.isPresent()){
                    dentistHolidayService.VerifyAndDisabled(holiday.get().getId(), idDentist);
                }

                covered = true;
                break;
            }

            if (!covered) {
                throw new BadRequestException("exception.dentistLockCalendarService.validateDentistAvailability.user", null, "exception.dentistLockCalendarService.validateDentistAvailability.log", new Object[]{idDentist, blockDate}, LogLevel.ERROR);
            }
        }
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
    private List<AppointmentConflictResponseDTO> verifyConflictsByDentistCalendarLock(DentistCalendarLockRequestCreateDTO dentistCalendarLockRequestCreateDTO, Dentist dentists) {

        //Obtiene turnos y turnos conflictivos.
        ConflictManagerContextInternalDTO context = prepareContextByCalendarLock(dentistCalendarLockRequestCreateDTO, dentists);

        //Retorna en caso de lista vacía. Caso contrario, persiste conflictos y retorna
        if (context.appointments().isEmpty() && context.appointmentConflicts().isEmpty()) {
            return Collections.emptyList();
        }

        //Persistimos en base solo los nuevos conflictos
        List<AppointmentConflict> conflictSaved = appointmentConflictService.create(context.appointmentConflicts());

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
     * Método privado del servicio.
     * Permite obtener turnos y turnos en conflicto.
     * Este método puede ser llamado por:
     * El método para detecta y persistir turnos en conflictos ante nuevo bloqueo de calendario.
     * El método que preview de Bloqueo de calendario, para consultar los posibles conflictos antes de crear
     */
    private ConflictManagerContextInternalDTO prepareContextByCalendarLock(DentistCalendarLockRequestCreateDTO dentistCalendarLockRequestCreateDTO, Dentist dentist){

        //Obtiene turno por dentista.
        List<Appointment> appointments = appointmentQueryService.getFutureAppointmentsReservedByDentist(dentist.getId());

        // Identificar si hay turnos en conflictos.
        List<AppointmentConflict> appointmentConflicts = evaluateAppointmentDentistCalendarLock(appointments, dentistCalendarLockRequestCreateDTO,dentistCalendarLockRequestCreateDTO.getRecurrence());

        return ConflictManagerContextInternalDTO.build(appointments, appointmentConflicts);

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

    private List<AppointmentConflict> evaluateAppointmentDentistCalendarLock(List<Appointment> appointments, DentistCalendarLockRequestCreateDTO dentistCalendarLockRequestCreateDTO, CalendarLockRecurrenceName recurrence) {
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






    /**
     * Método para verificar conflictos ante un preview de bloqueo de calendario dentista.
     *
     * @param dentistCalendarLockRequestCreateDTO :
     * @param dentists                            :
     */
    private List<AppointmentConflictResponseDTO> PreviewVerifyConflictsByDentistCalendarLock(DentistCalendarLockRequestCreateDTO dentistCalendarLockRequestCreateDTO, Dentist dentists) {

        //Obtiene turnos y turnos conflictivos.
        ConflictManagerContextInternalDTO context = prepareContextByCalendarLock(dentistCalendarLockRequestCreateDTO, dentists);

        //Retorna en caso de lista vacía. Caso contrario, persiste conflictos y retorna
        if (context.appointments().isEmpty() && context.appointmentConflicts().isEmpty()) {
            return Collections.emptyList();
        }


        //Mapea conflictos y devuelve
        return context.appointmentConflicts().stream()
                .map(AppointmentConflictResponseDTO::build)
                .toList();


    }





}
