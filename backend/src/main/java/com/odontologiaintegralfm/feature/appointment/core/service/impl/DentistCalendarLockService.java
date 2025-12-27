package com.odontologiaintegralfm.feature.appointment.core.service.impl;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.appointment.core.enums.CalendarLockRecurrenceName;
import com.odontologiaintegralfm.feature.appointment.catalogs.enums.DayName;
import com.odontologiaintegralfm.feature.appointment.catalogs.model.CalendarLockType;
import com.odontologiaintegralfm.feature.appointment.catalogs.service.ICalendarLockTypeService;
import com.odontologiaintegralfm.feature.appointment.core.dto.AppointmentConflictResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistCalendarLockRequestCreateDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistCalendarLockRequestUpdateDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistCalendarLockResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.enums.OriginConflict;
import com.odontologiaintegralfm.feature.appointment.core.model.DentistCalendarLock;
import com.odontologiaintegralfm.feature.appointment.core.model.DentistCalendarLockDetail;
import com.odontologiaintegralfm.feature.appointment.core.repository.IDentistCalendarLockRepository;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.IConflictManagerService;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.IDentistAvailabilityService;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.IDentistCalendarLockService;
import com.odontologiaintegralfm.feature.appointment.core.util.CalendarUtils;
import com.odontologiaintegralfm.feature.dentist.core.model.Dentist;
import com.odontologiaintegralfm.feature.dentist.core.service.interfaces.IDentistService;
import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.enums.LogType;
import com.odontologiaintegralfm.shared.exception.BadRequestException;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import com.odontologiaintegralfm.shared.dto.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.*;


@Service
public class DentistCalendarLockService implements IDentistCalendarLockService {

    private final IDentistService dentistService;
    private final ICalendarLockTypeService calendarLockTypeService;
    private final IDentistAvailabilityService dentistAvailabilityService;
    private final AuthenticatedUserService authenticatedUserService;
    private final IDentistCalendarLockRepository dentistLockCalendarRepository;
    private final IConflictManagerService conflictManagerService;
    private final MessageSource messageSource;
    private final DentistCalendarLockDetailService dentistCalendarLockDetailService;

    public DentistCalendarLockService(
            IDentistService dentistService,
            ICalendarLockTypeService calendarLockTypeService,
            IDentistAvailabilityService dentistAvailabilityService,
            AuthenticatedUserService authenticatedUserService,
            IDentistCalendarLockRepository dentistLockCalendarRepository,
            IConflictManagerService conflictManagerService,
            @Qualifier("messageSource") MessageSource messageSource,
            DentistCalendarLockDetailService dentistCalendarLockDetailService
    ) {
        this.dentistService = dentistService;
        this.calendarLockTypeService = calendarLockTypeService;
        this.dentistAvailabilityService = dentistAvailabilityService;
        this.authenticatedUserService = authenticatedUserService;
        this.dentistLockCalendarRepository = dentistLockCalendarRepository;
        this.conflictManagerService = conflictManagerService;
        this.messageSource = messageSource;
        this.dentistCalendarLockDetailService = dentistCalendarLockDetailService;
    }

    /**
     * Crea un nuevo bloqueo en el calendario de un dentista y valida posibles conflictos con turnos existentes.
     * <p>
     * Este método realiza los siguientes pasos:
     * <ol>
     *     <li>Obtiene el usuario y el dentista correspondiente al {@code idUser}.</li>
     *     <li>Valida que el tipo de bloqueo y la recurrencia sean correctos.</li>
     *     <li>Valida que la jornada laboral del dentista cubra el período del bloqueo.</li>
     *     <li>Verifica que la fecha de inicio no sea anterior a la fecha actual y que la fecha de fin no sea anterior a la de inicio.</li>
     *     <li>Crea y persiste el bloqueo en la base de datos.</li>
     *     <li>Asocia el ID del bloqueo y el origen de conflicto en el DTO.</li>
     *     <li>Verifica si existen turnos conflictivos y genera los {@link AppointmentConflictResponseDTO} correspondientes.</li>
     *     <li>Construye y retorna un {@link DentistCalendarLockResponseDTO} con la información del bloqueo y conflictos detectados.</li>
     * </ol>
     *
     * @param idPerson ID del Dentista
     * @param dentistCalendarLockRequestCreateDTO Datos del bloqueo a crear (fechas, horarios, días, observaciones, tipo y recurrencia).
     * @return {@link Response} que contiene el DTO del bloqueo creado y, si corresponde, los conflictos detectados.
     * @throws NotFoundException Si el dentista no se encuentra en la base de datos.
     * @throws ConflictException Si las fechas del bloqueo son inválidas (inicio antes de hoy o fin antes del inicio).
     * @throws DataBaseException Si ocurre un error al persistir los datos en la base.
     */

    @Override
    @Transactional
    @LogAction(
            value = "dentistCalendarLockService.logAction.create",
            args  = {"#idDentist", "#result.data.startDate", "#result.data.endDate","#result.data.recurrence","#result.data.conflict"},
            type  = LogType.SYSTEM,
            level = LogLevel.INFO
    )
    public Response<DentistCalendarLockResponseDTO> create(Long idPerson, DentistCalendarLockRequestCreateDTO dentistCalendarLockRequestCreateDTO) {
        try{

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

            //Validar Evento y tipos de datos de entrada para cada caso.
            CalendarLockType calendarLockType = calendarLockTypeService.getByIdInternal(dentistCalendarLockRequestCreateDTO.getIdLockType());
            validateLockType(calendarLockType, dentistCalendarLockRequestCreateDTO);

            //Valída y serializa recurrencia para casos diarios (que no envíe días y recurrencia).
            dentistCalendarLockRequestCreateDTO.setRecurrence(validateRecurrenceDays(dentistCalendarLockRequestCreateDTO.getRecurrence(),dentistCalendarLockRequestCreateDTO.getDays(), dentistCalendarLockRequestCreateDTO.getStartDate(),dentistCalendarLockRequestCreateDTO.getEndDate()));


            //Validación de recurrencia para casos semanales, quincenales, mensuales y anuales. Que la ventana de fechas de bloqueos al menos cubra la recurrencia enviada.
            if(dentistCalendarLockRequestCreateDTO.getRecurrence() != null){
                validateRecurrenceRange(dentistCalendarLockRequestCreateDTO.getRecurrence(),dentistCalendarLockRequestCreateDTO.getStartDate(),dentistCalendarLockRequestCreateDTO.getEndDate());
            }


            //Valída jornada laboral para casos de recurrencia NO diaria. Para los casos Daily(vacaciones) no se valida la jornada.
            if (dentistCalendarLockRequestCreateDTO.getRecurrence() != CalendarLockRecurrenceName.DAILY) {
                dentistAvailabilityService.validateCoverage(
                        idPerson,
                        generateEffectiveDates(
                                dentistCalendarLockRequestCreateDTO.getStartDate(),
                                dentistCalendarLockRequestCreateDTO.getEndDate(),
                                dentistCalendarLockRequestCreateDTO.getRecurrence(),
                                dentistCalendarLockRequestCreateDTO.getDays()
                        ),
                        dentistCalendarLockRequestCreateDTO.getStartTime(),
                        dentistCalendarLockRequestCreateDTO.getEndTime()
                );
            }

            //Valída que no exista otro bloqueo que sea misma Fecha inicio - fin - recurrencia - dias.
            verifyLockMatchWithLock(idPerson, dentistCalendarLockRequestCreateDTO);

            //Crea el bloqueo.
            DentistCalendarLock dentistCalendarLock = DentistCalendarLock.build(
                    dentists,
                    dentistCalendarLockRequestCreateDTO.getStartDate(),
                    dentistCalendarLockRequestCreateDTO.getEndDate(),
                    dentistCalendarLockRequestCreateDTO.getStartTime(),
                    dentistCalendarLockRequestCreateDTO.getEndTime(),
                    calendarLockType,
                    dentistCalendarLockRequestCreateDTO.getRecurrence(),
                    dentistCalendarLockRequestCreateDTO.getObservation()
            );
            //Setea campos de auditoria.
            dentistCalendarLock.setCreatedAt(LocalDateTime.now());
            dentistCalendarLock.setCreatedBy(authenticatedUserService.getAuthenticatedUser());
            dentistCalendarLock.setEnabled(true);

            DentistCalendarLock dentistCalendarLockSaved = dentistLockCalendarRepository.save(dentistCalendarLock);

            //Creo el detalle del bloqueo, si corresponde
            if (dentistCalendarLockRequestCreateDTO.getDays() != null && !dentistCalendarLockRequestCreateDTO.getDays().isEmpty()) {
                List<DentistCalendarLockDetail> dentistCalendarLockDetails = new ArrayList<>();
                for (DayName day : dentistCalendarLockRequestCreateDTO.getDays()){
                    DentistCalendarLockDetail detail = new DentistCalendarLockDetail();
                    detail.setDayName(day);
                    detail.setDentistCalendarLock(dentistCalendarLockSaved);
                    dentistCalendarLockDetails.add(detail);
                }

                dentistCalendarLockDetailService.saveAll(dentistCalendarLockDetails);
            }


            //Seteo recurrencia, id y origen de posible conflicto en el DTO.
            dentistCalendarLockRequestCreateDTO.setIdOriginConflict(dentistCalendarLockSaved.getId());
            dentistCalendarLockRequestCreateDTO.setOriginConflict(OriginConflict.DENTIST_CALENDAR_LOCK);

            //Validar si existen turnos conflictivos.
            List<AppointmentConflictResponseDTO> appointmentConflicts = conflictManagerService.verifyConflictsByDentistCalendarLock(dentistCalendarLockRequestCreateDTO,dentists);

            return new Response<>(
                    true,
                    (appointmentConflicts.isEmpty())
                            ? messageSource.getMessage("dentistCalendarLockService.create.ok.user",null, LocaleContextHolder.getLocale())
                            : messageSource.getMessage("dentistLockCalendarService.create.okWithConflict.user", null, LocaleContextHolder.getLocale()),
                    DentistCalendarLockResponseDTO.build(dentistCalendarLockSaved,appointmentConflicts)
            );

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "DentistCalendarLockService", idPerson, null, "create");
        }
    }




    /**
     * Actualiza un bloqueo existente en el calendario de un dentista.
     * <p>
     * El método realiza las siguientes operaciones:
     * <ol>
     *     <li>Recupera el bloqueo a actualizar a partir del ID proporcionado.</li>
     *     <li>Verifica que el bloqueo aún esté vigente; si ya finalizó, lanza una excepción de conflicto.</li>
     *     <li>Resuelve los turnos en conflicto posteriores a la finalización anticipada del bloqueo, si corresponde.</li>
     *     <li>Actualiza la información del bloqueo (por ejemplo, la observación de actualización y la fecha de fin).</li>
     *     <li>Mapea los datos actualizados a un {@link DentistCalendarLockResponseDTO} para la respuesta.</li>
     * </ol>
     *
     * @param dentistCalendarLockRequestUpdateDTO DTO que contiene los datos de actualización del bloqueo, incluyendo ID del bloqueo y observación de actualización.
     * @return {@link Response} que contiene el DTO con la información actualizada del bloqueo.
     * @throws BadRequestException Si el bloqueo con el ID proporcionado no se encuentra.
     * @throws ConflictException Si el bloqueo ya finalizó y no se puede actualizar.
     */

    @Override
    @LogAction(
            value = "dentistCalendarLockService.logAction.update",
            args  = {"#dentistCalendarLockRequestUpdateDTO.idDentistCalendarLock","#result.data.endDate","#result.data.ObservationUpdate"},
            type  = LogType.SYSTEM,
            level = LogLevel.INFO
    )
    public Response<DentistCalendarLockResponseDTO> update(DentistCalendarLockRequestUpdateDTO dentistCalendarLockRequestUpdateDTO) {

        //Recuperamos el Evento.
        DentistCalendarLock dentistCalendarLock = dentistLockCalendarRepository.findById(dentistCalendarLockRequestUpdateDTO.idDentistCalendarLock())
                .orElseThrow(()-> new BadRequestException("exception.dentistLockCalendarService.notFound.user", null,"exception.dentistLockCalendarService.notFound.log", new Object[]{dentistCalendarLockRequestUpdateDTO.idDentistCalendarLock(),"Dentist LockCalendar Service", "Update"}, LogLevel.ERROR));

        //Verificamos que esté vigente.
        if (dentistCalendarLock.getEndDate().isBefore(LocalDate.now()) ||
                (dentistCalendarLock.getEndDate().isEqual(LocalDate.now()) && !dentistCalendarLock.getEndTime().isAfter(LocalTime.now()))) {
            throw new ConflictException("exception.dentistLockCalendarService.validateLockBeforeNow.user", null, "exception.dentistLockCalendarService.validateLockBeforeNow.log", new Object[]{dentistCalendarLockRequestUpdateDTO.idDentistCalendarLock(), dentistCalendarLock.getEndDate(), dentistCalendarLock.getEndTime(),"Dentist CalendarLock Service", "Update"}, LogLevel.ERROR);
        }


        //Resuelve turnos en conflicto posterior a la finalización anticipada del bloqueo.
        conflictManagerService.resolvedAppointmentConflictByFinishLock(dentistCalendarLock);


        //Actualizamos la fecha de finalización del evento.
        DentistCalendarLock dentistCalendarLockSaved = finishCalendarLock(dentistCalendarLock,dentistCalendarLockRequestUpdateDTO.observationUpdate());


        return new Response<>(
                true,
                messageSource.getMessage("dentistCalendarLockService.update.ok.user",null, LocaleContextHolder.getLocale()),
                DentistCalendarLockResponseDTO.build(dentistCalendarLockSaved)
        );

    }




    /**
     * Método para obtener todos los bloqueos que corresponde solo a una fecha dada.
     *
     * @param dentistId : id dentista
     * @param date      : fecha a consulta por bloqueo.
     */
    @Override
    public List<DentistCalendarLock> getByDate(Long dentistId, LocalDate date) {


        //Recupera todos los bloqueos cuyo inicio sea <= y el fin sea => a una fecha dada.
        List<DentistCalendarLock> dentistCalendarLocks =  dentistLockCalendarRepository.findByDentistIdAndDateRange(dentistId,date);

        if(dentistCalendarLocks == null){
           return Collections.emptyList();
        }

        //Armo nueva lista solo con los bloqueos del día
        List<DentistCalendarLock> filteredDentistCalendarLocks = new ArrayList<>();

        for(DentistCalendarLock dentistCalendarLock : dentistCalendarLocks){

            //Bloqueo puntual
            if(dentistCalendarLock.getStartDate().equals(dentistCalendarLock.getEndDate())){
                filteredDentistCalendarLocks.add(dentistCalendarLock);
                continue;
            }

            //Bloqueos diarios (sin recurrencia o automático semana entera)
            List<DentistCalendarLockDetail> dentistCalendarLockDetails = dentistCalendarLockDetailService.getAllByDentistCalendarLock(dentistCalendarLock.getId());

            //Si él date recibido es la misma semana que el inicio "ancla" del bloqueo, comparo los días.
            if ((date.get(WeekFields.ISO.weekOfWeekBasedYear()) == dentistCalendarLock.getStartDate().get(WeekFields.ISO.weekOfWeekBasedYear()))) {

                dentistCalendarLockDetails.stream()
                        .forEach(details -> {
                            if (date.getDayOfWeek() == details.getDayName().toDayOfWeek()) {
                                filteredDentistCalendarLocks.add(dentistCalendarLock);
                            }
                        });

                continue;

            }


            //Bloqueo recurrente.
            for (DentistCalendarLockDetail details : dentistCalendarLockDetails) {
                if (dentistCalendarLock.getRecurrence().matches(
                        CalendarUtils.findFirstMatchingDate(dentistCalendarLock.getStartDate(), details.getDayName().toDayOfWeek()),
                        date
                )){
                    filteredDentistCalendarLocks.add(dentistCalendarLock);
                }
            }

        }
        return filteredDentistCalendarLocks;

    }









    /**
     * Valída si una fecha y hora se encuentran bloqueadas por un dentista.
     * Si existe, no realiza acción.
     * Si no existe, arroja exceptión.
     * @param idDentist : Id dentista
     * @param dateTime : Fecha y hora.
     */
    public void validateByIdDentistAndDateTime(Long idDentist, LocalDateTime dateTime) {

        //Obtener bloqueos.
        List<DentistCalendarLock> dentistCalendarLock = dentistLockCalendarRepository.findAllCurrentByDentistId(idDentist);

        //Validar esos bloqueos con la fecha del turno.
        for (DentistCalendarLock dc : dentistCalendarLock) {

            //Obtener detalles de bloqueos.
            List<DentistCalendarLockDetail> dentistCalendarLockDetails = dentistCalendarLockDetailService.getAllByDentistCalendarLock(dc.getId());

            //Si la lista está vacía, el bloqueo es diario.
            if (dentistCalendarLockDetails.isEmpty()) {
                if (CalendarUtils.isDateTimeWithinEvent(dateTime, dc.getStartDate(), dc.getEndDate(), null, dc.getStartTime(), dc.getEndTime(), CalendarLockRecurrenceName.DAILY)) {
                    throw new ConflictException("exception.validateByIdDentistAndDateTime.validateCalendarLock.user", null, "exception.validateByIdDentistAndDateTime.validateCalendarLock.log", new Object[]{dc.getId(),dc.getStartDate(),dc.getStartTime(),dc.getEndDate(),dc.getEndTime(), dateTime, "dentistCalendarLockService", "validateByIdDentistAndDateTime"}, LogLevel.ERROR);
                }
            } else {
                for (DentistCalendarLockDetail dcld : dentistCalendarLockDetails) {
                    if (CalendarUtils.isDateTimeWithinEvent(dateTime, dc.getStartDate(), dc.getEndDate(), dcld.getDayName().toDayOfWeek(), dc.getStartTime(), dc.getEndTime(), dc.getRecurrence())) {
                        throw new ConflictException("exception.validateByIdDentistAndDateTime.validateCalendarLock.user", null, "exception.validateByIdDentistAndDateTime.validateCalendarLock.log", new Object[]{dc.getId(), dateTime, "dentistCalendarLockService", "validateByIdDentistAndDateTime"}, LogLevel.ERROR);
                    }
                }
            }
        }
    }





    /**
     * Actualiza un bloqueo existente estableciendo su finalización al momento actual y registrando la observación de actualización.
     * <p>
     * Se actualizan los campos de auditoría ({@code updatedAt}, {@code updatedBy}) y se persiste el cambio en la base de datos.
     *
     * @param dentistCalendarLock Bloqueo de calendario a actualizar.
     * @param observationUpdate Observación que describe la actualización realizada.
     * @return El {@link DentistCalendarLock} actualizado y persistido.
     */

    private DentistCalendarLock finishCalendarLock(DentistCalendarLock dentistCalendarLock, String observationUpdate) {
        dentistCalendarLock.setEndDate(LocalDate.now());
        dentistCalendarLock.setEndTime(LocalTime.now());
        dentistCalendarLock.setUpdatedAt(LocalDateTime.now());
        dentistCalendarLock.setUpdatedBy(authenticatedUserService.getAuthenticatedUser());
        dentistCalendarLock.setObservationUpdate(observationUpdate);

        return dentistLockCalendarRepository.save(dentistCalendarLock);
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

    private List<LocalDate> generateEffectiveDates(
            LocalDate startDate,
            LocalDate endDate,
            CalendarLockRecurrenceName recurrence,
            List<DayName> days
    ) {
        List<LocalDate> dates = new ArrayList<>();

        for (LocalDate dayIteration = startDate; !dayIteration.isAfter(endDate); dayIteration = dayIteration.plusDays(1)) {

            // 1. Validar recurrencia (frecuencia)
            if (!CalendarUtils.matchesRecurrence(startDate, dayIteration, recurrence)) {
                continue;
            }

            // 2. Validar día de semana (si aplica)
            if (days != null && !days.isEmpty()) {
                DayName dayName = DayName.fromDayOfWeek(dayIteration.getDayOfWeek()); //Toma el ofWeek y retorna en formato DayName
                if (!days.contains(dayName)) {
                    continue;
                }
            }

            dates.add(dayIteration);
        }

        return dates;
    }



    /**
     * Valída que no exista ya un bloqueo de agenda con la misma configuración
     * (fecha inicio/fin, recurrencia y al menos un día en común).
     *
     * <p>La lógica funciona así:
     * <ul>
     *   <li>Obtiene todos los bloqueos vigentes del dentista.</li>
     *   <li>Compara la cabecera de cada bloqueo existente (startDate, endDate, recurrence)
     *       contra los valores enviados en la request.</li>
     *   <li>Si la cabecera no coincide, se ignora ese lock (continue).</li>
     *   <li>Si la cabecera coincide, se revisan los días asociados al lock.</li>
     *   <li>Si el bloqueo existente comparte al menos un día con el solicitado,
     *       se considera duplicado y se lanza una ConflictException.</li>
     * </ul>
     *
     * @param idPerson                            id del dentista que intenta crear el bloqueo
     * @param dentistCalendarLockRequestCreateDTO DTO con los datos del bloqueo solicitado
     * @throws ConflictException si ya existe otro bloqueo equivalente
     */
    @Override
    public void verifyLockMatchWithLock(Long idPerson, DentistCalendarLockRequestCreateDTO dentistCalendarLockRequestCreateDTO) {

        // Obtener todos los locks actuales del dentista
        List<DentistCalendarLock> existingLocks = dentistLockCalendarRepository.findAllCurrentByDentistId(idPerson);

        // Si no hay locks, no hay conflicto
        if (existingLocks.isEmpty()) {
            return;
        }

        // Fechas efectivas del nuevo bloqueo
        List<LocalDate> newBlockDates = generateEffectiveDates(
                dentistCalendarLockRequestCreateDTO.getStartDate(),
                dentistCalendarLockRequestCreateDTO.getEndDate(),
                dentistCalendarLockRequestCreateDTO.getRecurrence(),
                dentistCalendarLockRequestCreateDTO.getDays()
        );

        for (DentistCalendarLock lock : existingLocks) {

            //Obtengo los detalles del bloqueo.
            List<DentistCalendarLockDetail> dentistCalendarLockDetail = dentistCalendarLockDetailService.getAllByDentistCalendarLock(lock.getId());
            List<DayName> days = dentistCalendarLockDetail.stream()
                    .map(DentistCalendarLockDetail::getDayName)
                    .toList();

            Set<LocalDate> existingDates = new HashSet<>(generateEffectiveDates(
                    lock.getStartDate(),
                    lock.getEndDate(),
                    lock.getRecurrence(),
                    days
            ));

            existingDates.retainAll(newBlockDates);

            if (existingDates.isEmpty()) {
                continue;
            }


            // el nuevo termina antes de que empiece el existente o el nuevo empieza después de que termine el existente NO HAY SOLAPAMIENTO.
            boolean timeOverlap = !(dentistCalendarLockRequestCreateDTO.getEndTime().isBefore(lock.getStartTime()) || dentistCalendarLockRequestCreateDTO.getStartTime().isAfter(lock.getEndTime()));

            if (timeOverlap) {
                throw new ConflictException("exception.dentistCalendarLock.exist.user", new Object[]{lock.getId()}, "exception.dentistCalendarLock.exist.log", new Object[]{idPerson, lock.getId()}, LogLevel.ERROR);
            }
        }
    }





    /**
     * Valída la coherencia entre el tipo de bloqueo de calendario y los datos enviados en el request de creación del bloqueo.
     *
     * <p>
     * Este método aplica reglas estrictas cuando el tipo de evento
     * está marcado como {@code absenceTotal = true}, lo que representa ausencias
     * prolongadas y continuas (por ejemplo: vacaciones, licencias).
     * </p>
     *
     * <p><b>Reglas para tipos de bloqueo con ausencia total:</b></p>
     * <ul>
     *   <li>No se permiten días específicos ({@code days} debe ser {@code null}).</li>
     *   <li>La recurrencia solo puede ser {@code DAILY} o {@code NONE}.
     *       Cualquier otro valor es inválido.</li>
     *   <li>No se permiten horarios personalizados:
     *       {@code startTime} y {@code endTime} deben ser {@code null}.</li>
     * </ul>*
     * @param calendarLockType
     *        Tipo de bloqueo configurado en el catálogo de eventos.
     *        Define las reglas funcionales que deben cumplirse.
     *
     * @param dentistCalendarLockRequestCreateDTO
     *        DTO con los datos del bloqueo que se desea crear.
     *
     * @throws BadRequestException
     *         Si los datos enviados no son compatibles con el tipo de bloqueo,
     *         particularmente en eventos de ausencia total.
     */
    private void validateLockType(CalendarLockType calendarLockType, DentistCalendarLockRequestCreateDTO dentistCalendarLockRequestCreateDTO) {

        if (calendarLockType.isAllowTimeRange()) {
            if (dentistCalendarLockRequestCreateDTO.getStartTime() == null || dentistCalendarLockRequestCreateDTO.getEndTime() == null) {
                throw new BadRequestException("exception.dentistCalendarLockService.time.empty.user", null, "exception.dentistCalendarLockService.time.empty.log", new Object[]{calendarLockType.getName(), dentistCalendarLockRequestCreateDTO.getStartTime(), dentistCalendarLockRequestCreateDTO.getEndTime(), "DentistCalendarLockService", "validateLockType"}, LogLevel.ERROR);
            }
        } else {
            if (dentistCalendarLockRequestCreateDTO.getStartTime() != null || dentistCalendarLockRequestCreateDTO.getEndTime() != null) {
                throw new BadRequestException("exception.dentistCalendarLockService.time.user", null, "exception.dentistCalendarLockService.time.log", new Object[]{calendarLockType.getName(), dentistCalendarLockRequestCreateDTO.getStartTime(), dentistCalendarLockRequestCreateDTO.getEndTime(), "DentistCalendarLockService", "validateLockType"}, LogLevel.ERROR);
            }
        }

        if (calendarLockType.isAllowDays()) {
            if (dentistCalendarLockRequestCreateDTO.getDays() == null || dentistCalendarLockRequestCreateDTO.getDays().isEmpty()) {
                throw new BadRequestException("exception.dentistCalendarLockService.days.empty.user", null, "exception.dentistCalendarLockService.days.empty.log", new Object[]{calendarLockType.getName(), "DentistCalendarLockService", "validateLockType"}, LogLevel.ERROR);
            }
        } else {
            if (dentistCalendarLockRequestCreateDTO.getDays() != null && !dentistCalendarLockRequestCreateDTO.getDays().isEmpty() ) {
                throw new BadRequestException("exception.dentistCalendarLockService.days.user", null, "exception.dentistCalendarLockService.days.log", new Object[]{calendarLockType.getName(), "DentistCalendarLockService", "validateLockType"}, LogLevel.ERROR);
            }
        }

        //Si el evento tiene flag "ausencia total" entendemos que es un tipo de ausencia prolongada y de manera corrida.
        if (calendarLockType.isAbsenceTotal()) {
            if (dentistCalendarLockRequestCreateDTO.getDays() != null && !dentistCalendarLockRequestCreateDTO.getDays().isEmpty()) {
                throw new BadRequestException("exception.dentistCalendarLockService.days.user", null, "exception.dentistCalendarLockService.days.log", new Object[]{calendarLockType.getName(), "Dentist Calendar Lock Service", "validateLockType"}, LogLevel.ERROR);
            }

            if (dentistCalendarLockRequestCreateDTO.getStartTime() != null) {
                throw new BadRequestException("exception.dentistCalendarLockService.time.user", null, "exception.dentistCalendarLockService.time.log", new Object[]{calendarLockType.getName(), "Dentist Calendar Lock Service", "validateLockType"}, LogLevel.ERROR);
            }

            //Setea horarios para bloquear toda la jornada
            dentistCalendarLockRequestCreateDTO.setStartTime(LocalTime.MIDNIGHT);
            dentistCalendarLockRequestCreateDTO.setEndTime(LocalTime.of(23,59));
        }

    }






    /**
     * Valída la coherencia entre los días enviados y la recurrencia seleccionada para un bloqueo de agenda.
     *
     * <p>Reglas aplicadas:
     * <ul>
     *   <li><b>Si NO se envían días:</b>
     *       <ul>
     *         <li>Solo se permite recurrencia DAILY.</li>
     *         <li>Si no se envió recurrencia, se asigna DAILY por defecto.</li>
     *       </ul>
     *   </li>
     *   <li><b>Si se envían días:</b>
     *       <ul>
     *         <li>La recurrencia DAILY es inválida.</li>
     *         <li>Si no se envió recurrencia, se asigna NONE por defecto.</li>
     *       </ul>
     *   </li>
     * </ul>
     * @param recurrence recurrencia enviada en la request (puede ser null)
     * @param days lista de días enviados en la request (puede ser vacía o null)
     * @return la recurrencia final válida luego de aplicar las reglas
     * @throws BadRequestException si la combinación días/recurrencia es inválida
     */
    private CalendarLockRecurrenceName validateRecurrenceDays(CalendarLockRecurrenceName recurrence, List<DayName> days, LocalDate startDate, LocalDate endDate) {

        boolean noDays = (days == null || days.isEmpty());

        // Caso 1: NO hay días
        if (noDays) {

            // Bloqueo puntual
            if (startDate.equals(endDate)) {
                return CalendarLockRecurrenceName.NONE;
            }

            //Se acepta recurrencia NONE o DAILY
            if (recurrence == CalendarLockRecurrenceName.WEEKLY || recurrence == CalendarLockRecurrenceName.BIWEEKLY || recurrence == CalendarLockRecurrenceName.MONTHLY) {
                throw new BadRequestException("exception.dentistCalendarLockService.daysEmptyRecurrenceInvalid.user", null,"exception.dentistCalendarLockService.daysEmptyRecurrenceInvalid.log", new Object[]{days,recurrence,"DentistCalendarLockService", "validateRecurrenceDays"}, LogLevel.ERROR);
            }

            // Si no vino recurrencia y los días no son iguales-> asumimos DAILY
            return recurrence == null ? CalendarLockRecurrenceName.DAILY : recurrence;
        }

        //  Caso 2: HAY días -> DAILY es inválido
        if (recurrence == CalendarLockRecurrenceName.DAILY) {
            throw new BadRequestException("exception.dentistCalendarLockService.daysNotEmptyRecurrenceDaily.user", null,"exception.dentistCalendarLockService.daysNotEmptyRecurrenceDaily.log", new Object[]{days,recurrence,"DentistCalendarLockService", "validateRecurrenceDays"}, LogLevel.ERROR);
        }

        //  Caso 3: HAY días pero no vino recurrencia
        if ((recurrence == null) && (startDate.get(WeekFields.ISO.weekOfWeekBasedYear()) == endDate.get(WeekFields.ISO.weekOfWeekBasedYear()))) {
            return CalendarLockRecurrenceName.NONE;
        }

        // Caso 4: Hay días, no hay recurrencia, pero el inicio y fin son de diferentes semanas, se lanza exception.
        if ((recurrence == null) && (startDate.get(WeekFields.ISO.weekOfWeekBasedYear()) != endDate.get(WeekFields.ISO.weekOfWeekBasedYear()))) {
            throw new BadRequestException("exception.dentistCalendarLockService.startAndEndDifferentWeeks.user", null,"exception.dentistCalendarLockService.startAndEndDifferentWeeks.log", new Object[]{days,recurrence,startDate,endDate,"DentistCalendarLockService", "validateRecurrenceDays"}, LogLevel.ERROR);
        }

        return recurrence;
    }



    /**
     * Valída que el rango de fechas enviado (startDate - endDate) sea compatible con la recurrencia seleccionada para un bloqueo de agenda.
     * <p>Reglas:
     * <ul>
     *   <li><b>WEEKLY:</b> el rango debe cubrir al menos 7 días.</li>
     *   <li><b>BIWEEKLY:</b> el rango debe cubrir al menos 14 días.</li>
     *   <li><b>MONTHLY:</b> endDate debe ser al menos un mes posterior a startDate.</li>
     *   <li><b>YEARLY:</b> endDate debe ser al menos un año posterior a startDate.</li>
     * </ul>
     *
     * <p>Si el rango no cumple con la duración mínima requerida para la recurrencia,
     * se lanza una {@link BadRequestException}.
     *
     * @param recurrence tipo de recurrencia seleccionada (se asume no nulo)
     * @param startDate fecha de inicio del bloqueo
     * @param endDate fecha de fin del bloqueo
     * @throws BadRequestException si el rango de fechas es incompatible con la recurrencia
     */
    private void validateRecurrenceRange(CalendarLockRecurrenceName recurrence, LocalDate startDate, LocalDate endDate) {
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);

        switch (recurrence) {

            case WEEKLY -> {
                if (daysBetween < 7) {
                    throw new BadRequestException("exception.dentistCalendarLockService.recurrenceInvalid.weekly.user", null,"exception.dentistCalendarLockService.recurrenceInvalid.weekly.log", new Object[]{recurrence, startDate, endDate,"Dentist Calendar LockService","validateRecurrenceRange"}, LogLevel.ERROR);
                }
            }
            case BIWEEKLY -> {
                if (daysBetween < 14) {
                    throw new BadRequestException("exception.dentistCalendarLockService.recurrenceInvalid.biweekly.user", null,"exception.dentistCalendarLockService.recurrenceInvalid.biweekly.log", new Object[]{recurrence, startDate, endDate,"Dentist Calendar LockService","validateRecurrenceRange"}, LogLevel.ERROR);
                }
            }
            case MONTHLY -> {
                if (startDate.plusMonths(1).isAfter(endDate)){
                    throw new BadRequestException("exception.dentistCalendarLockService.recurrenceInvalid.monthly.user", null,"exception.dentistCalendarLockService.recurrenceInvalid.monthly.log", new Object[]{recurrence, startDate, endDate,"Dentist Calendar LockService","validateRecurrenceRange"}, LogLevel.ERROR);
                }
            }
            case YEARLY -> {
                if (startDate.plusYears(1).isAfter(endDate)){
                    throw new BadRequestException("exception.dentistCalendarLockService.recurrenceInvalid.yearly.user", null,"exception.dentistCalendarLockService.recurrenceInvalid.yearly.log", new Object[]{recurrence, startDate, endDate,"Dentist Calendar LockService","validateRecurrenceRange"}, LogLevel.ERROR);
                }
            }
        }


    }

}
