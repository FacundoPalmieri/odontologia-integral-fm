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
import com.odontologiaintegralfm.feature.appointment.core.model.AppointmentConflict;
import com.odontologiaintegralfm.feature.appointment.core.model.DentistAvailability;
import com.odontologiaintegralfm.feature.appointment.core.model.DentistCalendarLock;
import com.odontologiaintegralfm.feature.appointment.core.model.DentistCalendarLockDetail;
import com.odontologiaintegralfm.feature.appointment.core.repository.IDentistCalendarLockRepository;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.IConflictManagerService;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.IDentistCalendarLockService;
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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.*;


@Service
public class DentistCalendarLockService implements IDentistCalendarLockService {

    @Autowired
    private IDentistService dentistService;

    @Autowired
    private ICalendarLockTypeService calendarLockTypeService;

    @Autowired
    private DentistAvailabilityService dentistAvailabilityService;

    @Autowired
    private AuthenticatedUserService authenticatedUserService;

    @Autowired
    private AppointmentConflictService appointmentConflictService;

    @Autowired
    private IDentistCalendarLockRepository dentistLockCalendarRepository;


    @Autowired
    private IConflictManagerService conflictManagerService;

    @Qualifier("messageSource")
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private DentistCalendarLockDetailService dentistCalendarLockDetailService;

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

            //Valíd que la fecha de fin no sea anterior a la fecha de inicio.
            if(dentistCalendarLockRequestCreateDTO.getEndDate().isBefore(dentistCalendarLockRequestCreateDTO.getStartDate())){
                throw new ConflictException("exception.dentistLockCalendarService.validateEndDateBeforeStartDate.user",null,"exception.dentistLockCalendarService.validateEndDateBeforeStartDate.log",new Object[]{dentists.getId(),dentistCalendarLockRequestCreateDTO.getStartDate(),dentistCalendarLockRequestCreateDTO.getEndDate(),"Dentist Calendar Lock Service","create"},LogLevel.ERROR);
            }


            //Validar Evento
            CalendarLockType calendarLockType = calendarLockTypeService.getByIdInternal(dentistCalendarLockRequestCreateDTO.getIdLockType());

            //Validación de recurrencia
            if(dentistCalendarLockRequestCreateDTO.getRecurrence() != null){
                validateRecurrenceRange(dentistCalendarLockRequestCreateDTO.getRecurrence(),dentistCalendarLockRequestCreateDTO.getStartDate(),dentistCalendarLockRequestCreateDTO.getEndDate());
            }

            //Valída casos diarios (que no envíe días y recurrencia).
            dentistCalendarLockRequestCreateDTO.setRecurrence(validateRecurrenceDays(dentistCalendarLockRequestCreateDTO.getRecurrence(),dentistCalendarLockRequestCreateDTO.getDays(), dentistCalendarLockRequestCreateDTO.getStartDate(),dentistCalendarLockRequestCreateDTO.getEndDate()));

            //Validar Jornada laboral.
            validateAvailabilityForCalendarLock(idPerson,dentistCalendarLockRequestCreateDTO.getStartDate(), dentistCalendarLockRequestCreateDTO.getEndDate(), dentistCalendarLockRequestCreateDTO.getStartTime(), dentistCalendarLockRequestCreateDTO.getEndTime(),dentistCalendarLockRequestCreateDTO.getDays(), dentistCalendarLockRequestCreateDTO.getRecurrence());



            //Valída que no exista otro bloqueo que sea misma Fecha inicio - fin - recurrencia - dias.
            validateLock(idPerson, dentistCalendarLockRequestCreateDTO);

            //Crea el bloqueo.
            DentistCalendarLock dentistCalendarLock = new DentistCalendarLock();
            dentistCalendarLock.setDentist(dentists);
            dentistCalendarLock.setStartDate(dentistCalendarLockRequestCreateDTO.getStartDate());
            dentistCalendarLock.setEndDate(dentistCalendarLockRequestCreateDTO.getEndDate());
            dentistCalendarLock.setStartTime(dentistCalendarLockRequestCreateDTO.getStartTime());
            dentistCalendarLock.setEndTime(dentistCalendarLockRequestCreateDTO.getEndTime());
            dentistCalendarLock.setType(calendarLockType);
            dentistCalendarLock.setRecurrence(dentistCalendarLockRequestCreateDTO.getRecurrence());
            dentistCalendarLock.setObservation( dentistCalendarLockRequestCreateDTO.getObservation());
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




            //Mapea la respuesta al DTO.
            DentistCalendarLockResponseDTO dentistCalendarLockResponseDTO = new DentistCalendarLockResponseDTO(
                    dentistCalendarLockSaved.getId(),
                    dentistCalendarLockSaved.getDentist().getId(),
                    dentistCalendarLockSaved.getType().getName(),
                    dentistCalendarLockSaved.getRecurrence().getLabel(),
                    dentistCalendarLockSaved.getStartDate(),
                    dentistCalendarLockSaved.getEndDate(),
                    dentistCalendarLockSaved.getStartTime(),dentistCalendarLockSaved.getEndTime(),
                    dentistCalendarLockSaved.getObservation(),
                    dentistCalendarLockSaved.getObservationUpdate(),
                    appointmentConflicts
            );


            return new Response<>(
                    true,
                    (appointmentConflicts.isEmpty())
                            ? messageSource.getMessage("dentistCalendarLockService.create.ok.user",null, LocaleContextHolder.getLocale())
                            : messageSource.getMessage("dentistLockCalendarService.create.okWithConflict.user", null, LocaleContextHolder.getLocale()),
                    dentistCalendarLockResponseDTO
            );

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "DentistCalendarLockService", idPerson, null, "create");
        }
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
     * @param idPerson id del dentista que intenta crear el bloqueo
     * @param dentistCalendarLockRequestCreateDTO DTO con los datos del bloqueo solicitado
     * @throws ConflictException si ya existe otro bloqueo equivalente
     */

    private void validateLock(Long idPerson, DentistCalendarLockRequestCreateDTO dentistCalendarLockRequestCreateDTO) {


        // Obtener todos los locks actuales del dentista
        List<DentistCalendarLock> dentistCalendarLocks = dentistLockCalendarRepository.findAllCurrentByDentistId(idPerson);

        // Conjunto de días que enviaron en la request
        Set<DayName> requestDays = new HashSet<>(dentistCalendarLockRequestCreateDTO.getDays());

        for (DentistCalendarLock lock : dentistCalendarLocks) {

            // Coincidencia con la cabecera del bloqueo
            boolean match =
                    lock.getStartDate().equals(dentistCalendarLockRequestCreateDTO.getStartDate()) &&
                            lock.getEndDate().equals(dentistCalendarLockRequestCreateDTO.getEndDate()) &&
                            lock.getRecurrence().equals(dentistCalendarLockRequestCreateDTO.getRecurrence());

            if (!match) {
                continue;
            }

            // Obtener los detalles del lock existente
            List<DayName> lockDays = dentistCalendarLockDetailService.getAllByDentistCalendarLock(lock.getId())
                    .stream()
                    .map(DentistCalendarLockDetail::getDayName)
                    .toList();

            // Verificar si comparten al menos un día
            boolean overlap = lockDays.stream().anyMatch(requestDays::contains);

            if (overlap) {
                throw new ConflictException("exception.dentistCalendarLock.exist.user", null, "exception.dentistCalendarLock.exist.log", new Object[]{idPerson, lock.getId(),},LogLevel.ERROR
                );
            }
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

            if (recurrence != null && recurrence != CalendarLockRecurrenceName.DAILY) {
                throw new BadRequestException("exception.dentistCalendarLockService.daysEmptyRecurrenceInvalid.user", null,"exception.dentistCalendarLockService.daysEmptyRecurrenceInvalid.log", new Object[]{days,recurrence,"DentistCalendarLockService", "validateRecurrenceDays"}, LogLevel.ERROR);
            }

            // Si no vino recurrencia -> asumimos DAILY
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

        // Caso 4: Hay días, no hay recurrencia, pero el inicio y fin son de diferentes semanas
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
        validateAppointmentConflict(dentistCalendarLock);


        //Actualizamos la fecha de finalización del evento.
        DentistCalendarLock dentistCalendarLockSaved = updateCalendarLock(dentistCalendarLock,dentistCalendarLockRequestUpdateDTO.observationUpdate());

        //Mapeamos el DTO para respuesta.
        DentistCalendarLockResponseDTO dentistCalendarLockResponseDTO = new DentistCalendarLockResponseDTO(
                dentistCalendarLockSaved.getId(),
                dentistCalendarLockSaved.getDentist().getId(),
                dentistCalendarLockSaved.getType().getName(),
                dentistCalendarLockSaved.getRecurrence().getLabel(),
                dentistCalendarLockSaved.getStartDate(),
                dentistCalendarLockSaved.getEndDate(),
                dentistCalendarLockSaved.getStartTime(),
                dentistCalendarLockSaved.getEndTime(),
                dentistCalendarLockSaved.getObservation(),
                dentistCalendarLockSaved.getObservationUpdate(),
                null
        );

        return new Response<>(
                true,
                messageSource.getMessage("dentistCalendarLockService.update.ok.user",null, LocaleContextHolder.getLocale()),
                dentistCalendarLockResponseDTO
        );

    }







    /**
     * Método para obtener todos los bloqueos de calendario por Id dentista.
     *
     * @param idDentist : id dentista
     */
    @Override
    public List<DentistCalendarLock> getAllCurrentByDentistId(Long idDentist) {
        try{
            return dentistLockCalendarRepository.findAllCurrentByDentistId(idDentist);
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "DentistCalendarLockService", idDentist, null, "getAllByDentistId");
        }
    }



    /**
     * Método para obtener todos los bloqueos verificando que el inicio sea <= y el fin sea => a una fecha dada.
     * @param dentistId : id dentista
     */
    @Override
    public List<DentistCalendarLock> getByDentistIdAndDateRange(Long dentistId, LocalDate date) {
        return dentistLockCalendarRepository.findByDentistIdAndDateRange(dentistId,date);
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
            return null;
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
            for(DentistCalendarLockDetail details : dentistCalendarLockDetails) {
                if(conflictManagerService.validateRecurrence(
                        dentistCalendarLock.getRecurrence(),
                        conflictManagerService.findFirstMatchingDate(dentistCalendarLock.getStartDate(),details.getDayName().toDayOfWeek()),
                        date)
                ){
                    filteredDentistCalendarLocks.add(dentistCalendarLock);
                }
            }

        }
        return filteredDentistCalendarLocks;

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

    private DentistCalendarLock updateCalendarLock(DentistCalendarLock dentistCalendarLock,String observationUpdate) {
        dentistCalendarLock.setEndDate(LocalDate.now());
        dentistCalendarLock.setEndTime(LocalTime.now());
        dentistCalendarLock.setUpdatedAt(LocalDateTime.now());
        dentistCalendarLock.setUpdatedBy(authenticatedUserService.getAuthenticatedUser());
        dentistCalendarLock.setObservationUpdate(observationUpdate);

        return dentistLockCalendarRepository.save(dentistCalendarLock);
    }






    /**
     * Valida y resuelve los turnos en conflicto cuando un bloqueo de calendario se finaliza anticipadamente.
     * <p>
     * El método obtiene todos los conflictos asociados al bloqueo y, si existen, los marca como resueltos
     * mediante el servicio de gestión de conflictos.
     *
     * @param dentistCalendarLock Bloqueo de calendario que se está finalizando anticipadamente.
     */

    private void validateAppointmentConflict(DentistCalendarLock dentistCalendarLock) {

        List<AppointmentConflict> appointmentConflicts = appointmentConflictService.getAllByDentistIdAndCalendarLockConflictId(dentistCalendarLock.getDentist().getId(), dentistCalendarLock.getId());

        if(!appointmentConflicts.isEmpty()){
            conflictManagerService.updateResolvedConflicts(dentistCalendarLock.getDentist().getId(),appointmentConflicts);
        }

    }





    /**
     * Valída que un bloqueo de calendario propuesto coincida con la jornada laboral del dentista.
     *
     * <p>Dependiendo del tipo de recurrencia del bloqueo:</p>
     * <ul>
     *     <li><b>DAILY:</b> El bloqueo debe cubrir todos los días laborales del dentista dentro del rango de fechas.</li>
     *     <li><b>WEEKLY / MONTHLY / YEARLY / NONE:</b> El bloqueo debe coincidir con al menos un día laboral del dentista.</li>
     * </ul>
     *
     * <p>El método obtiene las disponibilidades del dentista y verifica si las fechas y horarios del bloqueo
     * están completamente cubiertos según la recurrencia.</p>
     *
     * @param idDentist Id del dentista cuyo calendario se valida.
     * @param startDateBlock Semana de inicio del bloqueo.
     * @param endDateBlock Semana de fin del bloqueo.
     * @param starTimeBlock Hora de inicio del bloqueo.
     * @param endTimeBlock Hora de fin del bloqueo.
     * @param daysBlock Lista de días del bloqueo (DayOfWeek).
     * @param recurrenceBlock Tipo de recurrencia del bloqueo.
     *
     * @throws BadRequestException Si el bloqueo no cumple con la cobertura requerida según la recurrencia y jornada del dentista.
     */

    private void validateAvailabilityForCalendarLock(Long idDentist, LocalDate startDateBlock, LocalDate endDateBlock, LocalTime starTimeBlock, LocalTime endTimeBlock, List<DayName> daysBlock, CalendarLockRecurrenceName recurrenceBlock) {
        List<DentistAvailability> availabilities = dentistAvailabilityService.getByIdInternal(idDentist);

        // Si no especifican días, usar toda la semana
        List<DayOfWeek> daysToEvaluate =
                (daysBlock == null || daysBlock.isEmpty())
                        ? Arrays.asList(DayOfWeek.values())
                        : convertToDayOfWeek(daysBlock);

        for (DayOfWeek day : daysToEvaluate) {
            LocalDate effectiveStartDateBlock = conflictManagerService.findFirstMatchingDate(startDateBlock, day);
            LocalDate effectiveEndDateBlock = conflictManagerService.findLastMatchingDate(endDateBlock, day);
            boolean valid = conflictManagerService.hasBlockMatchWithAvailability(availabilities, effectiveStartDateBlock, effectiveEndDateBlock, starTimeBlock, endTimeBlock, recurrenceBlock);
            if (!valid) {
                throw new BadRequestException("exception.dentistLockCalendarService.validateDentistAvailability.user", null, "exception.dentistLockCalendarService.validateDentistAvailability.log", new Object[]{idDentist, "DentistCalendarLockService", "validateDentistAvailability"}, LogLevel.ERROR);
            }
        }

    }



    private List<DayOfWeek> convertToDayOfWeek(List<DayName> days){
        return days.stream()
                .map(DayName::toDayOfWeek)
                .toList();
    }
}
