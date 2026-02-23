package com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.service;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.appointmentscheduling.calendar.enums.CalendarLockRecurrenceName;
import com.odontologiaintegralfm.feature.appointmentscheduling.shared.DayName;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.model.DentistCalendarLock;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.model.DentistCalendarLockDetail;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.repository.IDentistCalendarLockRepository;
import com.odontologiaintegralfm.feature.appointmentscheduling.calendar.util.CalendarUtils;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.dto.DentistCalendarLockContextInternalDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.dto.DentistCalendarLockRequestCreateDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.dto.DentistCalendarLockRequestUpdateDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.dto.DentistCalendarLockResponseDTO;
import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.enums.LogType;
import com.odontologiaintegralfm.shared.exception.BadRequestException;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import com.odontologiaintegralfm.shared.dto.Response;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.*;


@Service
public class DentistCalendarLockService implements IDentistCalendarLockService {

    private final AuthenticatedUserService authenticatedUserService;
    private final IDentistCalendarLockRepository dentistLockCalendarRepository;
    private final DentistCalendarLockDetailService dentistCalendarLockDetailService;


    public DentistCalendarLockService(
            AuthenticatedUserService authenticatedUserService,
            IDentistCalendarLockRepository dentistLockCalendarRepository,
            @Qualifier("messageSource") MessageSource messageSource,
            DentistCalendarLockDetailService dentistCalendarLockDetailService) {
        this.authenticatedUserService = authenticatedUserService;
        this.dentistLockCalendarRepository = dentistLockCalendarRepository;
        this.dentistCalendarLockDetailService = dentistCalendarLockDetailService;
    }


    /**
     * Persiste un bloqueo de calendario de dentista en la base de datos, junto con sus detalles
     * de recurrencia si se proporcionan días específicos. Además, actualiza el DTO con información
     * sobre el origen de posibles conflictos para su posterior uso.
     *
     * @param dentistCalendarLock                DTO interno que contiene el bloqueo de calendario y su contexto.
     * @param dentistCalendarLockRequestCreateDTO DTO que contiene los datos de creación del bloqueo, incluyendo días de recurrencia.
     * @return DentistCalendarLock instancia persistida del bloqueo de calendario.
     * @throws DataBaseException si ocurre un error al guardar el bloqueo en la base de datos.
     */

    @Override
    public  DentistCalendarLock create(DentistCalendarLockContextInternalDTO dentistCalendarLock, DentistCalendarLockRequestCreateDTO dentistCalendarLockRequestCreateDTO) {
        try{

            //Se persiste
            DentistCalendarLock dentistCalendarLockSaved = dentistLockCalendarRepository.save(dentistCalendarLock.dentistCalendarLock());


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

            return dentistCalendarLockSaved;

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "DentistCalendarLockService", null, null, "create");
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
    public DentistCalendarLock update(DentistCalendarLockRequestUpdateDTO dentistCalendarLockRequestUpdateDTO) {

        //Recuperamos el Evento.
        DentistCalendarLock dentistCalendarLock = dentistLockCalendarRepository.findById(dentistCalendarLockRequestUpdateDTO.idDentistCalendarLock())
                .orElseThrow(()-> new BadRequestException("exception.dentistLockCalendarService.notFound.user", null,"exception.dentistLockCalendarService.notFound.log", new Object[]{dentistCalendarLockRequestUpdateDTO.idDentistCalendarLock(),"Dentist LockCalendar Service", "Update"}, LogLevel.ERROR));


        //Verificamos que esté vigente.
        if (dentistCalendarLock.getEndDate().isBefore(LocalDate.now()) ||
                (dentistCalendarLock.getEndDate().isEqual(LocalDate.now()) && !dentistCalendarLock.getEndTime().isAfter(LocalTime.now()))) {
            throw new ConflictException("exception.dentistLockCalendarService.validateLockBeforeNow.user", null, "exception.dentistLockCalendarService.validateLockBeforeNow.log", new Object[]{dentistCalendarLockRequestUpdateDTO.idDentistCalendarLock(), dentistCalendarLock.getEndDate(), dentistCalendarLock.getEndTime(),"Dentist CalendarLock Service", "Update"}, LogLevel.ERROR);
        }


        //Actualizamos la fecha de finalización del evento.
        return finishCalendarLock(dentistCalendarLock,dentistCalendarLockRequestUpdateDTO.observationUpdate());

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
        List<DentistCalendarLock> dentistCalendarLocks =  dentistLockCalendarRepository.findByDentistIdAndDate(dentistId,date);

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

                if(dentistCalendarLockDetails.isEmpty()){
                    filteredDentistCalendarLocks.add(dentistCalendarLock);
                }else{
                    dentistCalendarLockDetails.stream()
                            .forEach(details -> {
                                if (date.getDayOfWeek() == details.getDayName().toDayOfWeek()) {
                                    filteredDentistCalendarLocks.add(dentistCalendarLock);
                                }
                            });

                    continue;

                }

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
     * Método para obtener todos los bloqueos que corresponde a una semana
     *
     * @param dentistId : id dentista
     * @param weekStart : Fecha inicio semana a consulta por bloqueo.
     * @param weekEnd   : Fecha fin semana a consulta por bloqueo.
     */
    @Override
    public Map<LocalDate, List<DentistCalendarLock>> getByDateRange(Long dentistId, LocalDate weekStart, LocalDate weekEnd) {
        List<DentistCalendarLock> locks = dentistLockCalendarRepository.findByDentistIdAndDateRange(dentistId, weekStart, weekEnd);


        Map<LocalDate, List<DentistCalendarLock>> locksByDay = new HashMap<>();


        for (DentistCalendarLock lock : locks) {

            //Obtengo detalles por cada bloqueo.
           List <DentistCalendarLockDetail> dentistCalendarLockDetail = dentistCalendarLockDetailService.getAllByDentistCalendarLock(lock.getId());

            List<LocalDate> effectiveDates =
                    generateEffectiveDates(
                            lock.getStartDate(),    // ancla real
                            weekStart,
                            lock.getEndDate(),
                            lock.getRecurrence(),
                            dentistCalendarLockDetail.stream()
                                    .map(DentistCalendarLockDetail::getDayName)
                                    .toList()
                    );

            for (LocalDate date : effectiveDates) {
                locksByDay
                        .computeIfAbsent(date, d -> new ArrayList<>())
                        .add(lock);
            }
        }

        return locksByDay;
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
        List<DentistCalendarLock> dentistCalendarLock = dentistLockCalendarRepository.findByDentistIdAndDate(idDentist, dateTime.toLocalDate());

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
     * Genera la lista de fechas efectivas para un bloqueo de agenda, en función
     * de un rango de fechas, un tipo de recurrencia y, opcionalmente, una lista
     * de días de la semana.
     *
     * <p>El método recorre el rango comprendido entre {@code startDate} y
     * {@code endDate} (inclusive) y construye las fechas en las que el bloqueo
     * debe aplicarse.</p>
     *
     * <h3>Reglas de funcionamiento</h3>
     *
     * <ul>
     *   <li>
     *     <b>Recurrencia</b>:
     *     La validación de la recurrencia se delega al enum
     *     {@link CalendarLockRecurrenceName} mediante el método
     *     {@code matches(startDate, currentDate)}.
     *   </li>
     *
     *   <li>
     *     <b>Días de la semana</b>:
     *     Si se especifican días ({@code days}), el método calcula una fecha
     *     de inicio y fin efectiva para cada día, alineando el rango al día
     *     correspondiente antes de evaluar la recurrencia.
     *   </li>
     *
     *   <li>
     *     <b>Separación de responsabilidades</b>:
     *     <ul>
     *       <li>Este método coordina el cálculo de fechas.</li>
     *       <li>El enum define las reglas de recurrencia.</li>
     *       <li>Los utilitarios de fechas alinean los días del rango.</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * <h3>Comportamiento según parámetros</h3>
     *
     * <ul>
     *   <li>
     *     Si {@code days} es {@code null} o vacío, se ignora la validación
     *     por día de la semana y solo se evalúa la recurrencia.
     *   </li>
     *   <li>
     *     Si {@code days} contiene valores, el método:
     *     <ol>
     *       <li>Calcula la primera fecha válida para cada día.</li>
     *       <li>Calcula la última fecha válida para cada día.</li>
     *       <li>Evalúa la recurrencia usando una fecha base alineada.</li>
     *     </ol>
     *   </li>
     * </ul>
     *
     * <h3>Notas importantes</h3>
     *
     * <ul>
     *   <li>
     *     El método <b>no modifica</b> la fecha de inicio original del bloqueo,
     *     sino que utiliza fechas efectivas internas para evaluar la recurrencia.
     *   </li>
     *   <li>
     *     No valida superposición de bloqueos ni reglas de negocio externas.
     *   </li>
     *   <li>
     *     El rango de fechas se considera inclusivo.
     *   </li>
     * </ul>
     * @param realStartDate fecha real de inicio del bloqueo.
     * @param startDate fecha de inicio a evaluar (varía según la vista)
     * @param endDate fecha de fin a evaluar del bloqueo (varía según la vista)
     * @param recurrence tipo de recurrencia del bloqueo.
     * @param days lista opcional de días de la semana en los que aplica el bloqueo.
     *
     * @return lista de fechas en las que el bloqueo es efectivo.
     *
     * @throws NullPointerException si {@code startDate}, {@code endDate} o
     *         {@code recurrence} son {@code null}.
     */

    public List<LocalDate> generateEffectiveDates(
            LocalDate realStartDate,  // Ancla real
            LocalDate startDate,      // Inicio del rango a evaluar (para cada vista)
            LocalDate endDate,        // Fin del evento
            CalendarLockRecurrenceName recurrence,
            List<DayName> days
    ) {

        // Lista final de fechas efectivas del bloqueo
        List<LocalDate> dates = new ArrayList<>();


        /*
         * CASO 1:
         * No se especificaron días de la semana.
         * Ej: bloqueo puntual, bloqueo diario, etc.
         *
         * En este caso:
         * - Recorremos todas las fechas entre startDate y endDate
         * - Delegamos la decisión de si una fecha aplica o no
         *   exclusivamente al enum (recurrence.matches)
         */
        if (days == null || days.isEmpty()) {

            for (LocalDate dayIteration = startDate;
                 !dayIteration.isAfter(endDate);
                 dayIteration = dayIteration.plusDays(1)) {

                if (!recurrence.matches(realStartDate, dayIteration)) {
                    continue;
                }

                dates.add(dayIteration);
            }

            return dates;
        }

        /*
         * CASO 2:
         * Hay días de la semana configurados (ej: MONDAY, WEDNESDAY).
         *
         * IMPORTANTE:
         * El enum WEEKLY asume que startDate es el día base.
         * Como startDate puede NO coincidir con el día pedido,
         * se calcula la primer fecha dentro de los próximos 7 dias que coincide con el DAY
         */
        for (DayName day : days) {

            // Convertimos nuestro DayName a DayOfWeek de Java
            DayOfWeek dayOfWeek = day.toDayOfWeek();


            /*
             * Calculamos la PRIMER fecha >= startDate
             * que coincida con este día de la semana.
             *
             * Ej:
             * startDate = 27/12 (sábado)
             * day = MONDAY
             * effectiveStart = 29/12
             */
            LocalDate effectiveStart = CalendarUtils.findFirstMatchingDate(startDate, dayOfWeek);


            /*
             * Calculamos la ÚLTIMA fecha <= endDate
             * que coincida con este día de la semana.
             */
            LocalDate effectiveEnd = CalendarUtils.findLastMatchingDate(endDate, dayOfWeek);

            for (LocalDate dayIteration = effectiveStart; !dayIteration.isAfter(effectiveEnd); dayIteration = dayIteration.plusWeeks(1)) {

                /*
                 * Validamos la recurrencia usando el enum, PERO usando effectiveStart como fecha base */
                if (!recurrence.matches(realStartDate, dayIteration)) {
                    continue;
                }
                dates.add(dayIteration);
            }
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
     * </ul>**
     * @param dentistCalendarLockRequestCreateDTO
     *        DTO con el modo del bloqueo que se desea crear.
     *
     * @throws BadRequestException
     *         Si los datos enviados no son compatibles con el tipo de bloqueo,
     *         particularmente en eventos de ausencia total.
     */
    public void validateByMode(DentistCalendarLockRequestCreateDTO dentistCalendarLockRequestCreateDTO) {


        switch (dentistCalendarLockRequestCreateDTO.getMode()) {
            case POINTUAL -> {

                if (!dentistCalendarLockRequestCreateDTO.getStartDate().equals(dentistCalendarLockRequestCreateDTO.getEndDate())) {
                    throw new BadRequestException("exception.dentistCalendarLockService.pointual.dateEquals.user", null, "exception.dentistCalendarLockService.pointual.dateEquals.log", new Object[]{dentistCalendarLockRequestCreateDTO.getMode(), dentistCalendarLockRequestCreateDTO.getStartDate(), dentistCalendarLockRequestCreateDTO.getEndDate(), "DentistCalendarLockService", "validateByMode"}, LogLevel.ERROR);
                }
                if (dentistCalendarLockRequestCreateDTO.getRecurrence() != CalendarLockRecurrenceName.NONE) {
                    throw new BadRequestException("exception.dentistCalendarLockService.pointual.recurrence.user", null, "exception.dentistCalendarLockService.pointual.recurrence.log", new Object[]{dentistCalendarLockRequestCreateDTO.getMode(), dentistCalendarLockRequestCreateDTO.getRecurrence(), "DentistCalendarLockService", "validateByMode"}, LogLevel.ERROR);
                }
                if (dentistCalendarLockRequestCreateDTO.getDays() != null && !dentistCalendarLockRequestCreateDTO.getDays().isEmpty()) {
                    throw new BadRequestException("exception.dentistCalendarLockService.pointual.days.user", null, "exception.dentistCalendarLockService.pointual.days.log", new Object[]{dentistCalendarLockRequestCreateDTO.getMode(), "DentistCalendarLockService", "validateByMode"}, LogLevel.ERROR);
                }

            }


            case DAYS_IN_RANGE_NO_RECURRENCE -> {
                if (dentistCalendarLockRequestCreateDTO.getStartDate().equals(dentistCalendarLockRequestCreateDTO.getEndDate())) {
                    throw new BadRequestException("exception.dentistCalendarLockService.daysRangeNoRecurrence.date.user", null, "exception.dentistCalendarLockService.daysRangeNoRecurrence.date.log", new Object[]{dentistCalendarLockRequestCreateDTO.getMode(), dentistCalendarLockRequestCreateDTO.getStartDate(), dentistCalendarLockRequestCreateDTO.getEndDate(), "DentistCalendarLockService", "validateByMode"}, LogLevel.ERROR);
                }
                if (dentistCalendarLockRequestCreateDTO.getRecurrence() != CalendarLockRecurrenceName.NONE) {
                    throw new BadRequestException("exception.dentistCalendarLockService.daysRangeNoRecurrence.recurrence.user", null, "exception.dentistCalendarLockService.daysRangeNoRecurrence.recurrence.log", new Object[]{dentistCalendarLockRequestCreateDTO.getMode(), dentistCalendarLockRequestCreateDTO.getRecurrence(), "DentistCalendarLockService", "validateByMode"}, LogLevel.ERROR);
                }
                if (dentistCalendarLockRequestCreateDTO.getDays() == null || dentistCalendarLockRequestCreateDTO.getDays().isEmpty()) {
                    throw new BadRequestException("exception.dentistCalendarLockService.daysRangeNoRecurrence.days.user", null, "exception.dentistCalendarLockService.daysRangeNoRecurrence.days.log", new Object[]{dentistCalendarLockRequestCreateDTO.getMode(), "DentistCalendarLockService", "validateByMode"}, LogLevel.ERROR);
                }
            }


            case DAILY_CONTINUOUS -> {
                if (dentistCalendarLockRequestCreateDTO.getStartDate().equals(dentistCalendarLockRequestCreateDTO.getEndDate())) {
                    throw new BadRequestException("exception.dentistCalendarLockService.daily.date.user", null, "exception.dentistCalendarLockService.daily.date.log", new Object[]{dentistCalendarLockRequestCreateDTO.getMode(), dentistCalendarLockRequestCreateDTO.getStartDate(), dentistCalendarLockRequestCreateDTO.getEndDate(), "DentistCalendarLockService", "validateByMode"}, LogLevel.ERROR);
                }
                if (dentistCalendarLockRequestCreateDTO.getRecurrence() != CalendarLockRecurrenceName.DAILY) {
                    throw new BadRequestException("exception.dentistCalendarLockService.daily.recurrence.user", null, "exception.dentistCalendarLockService.daily.recurrence.log", new Object[]{dentistCalendarLockRequestCreateDTO.getMode(), dentistCalendarLockRequestCreateDTO.getRecurrence(), "DentistCalendarLockService", "validateByMode"}, LogLevel.ERROR);
                }
                if (dentistCalendarLockRequestCreateDTO.getDays() != null && !dentistCalendarLockRequestCreateDTO.getDays().isEmpty()) {
                    throw new BadRequestException("exception.dentistCalendarLockService.daily.days.user", null, "exception.dentistCalendarLockService.daily.days.log", new Object[]{dentistCalendarLockRequestCreateDTO.getMode(), "DentistCalendarLockService", "validateByMode"}, LogLevel.ERROR);

                }
            }


            case RECURRENT_PATTERN -> {
                if (dentistCalendarLockRequestCreateDTO.getStartDate().equals(dentistCalendarLockRequestCreateDTO.getEndDate())) {
                    throw new BadRequestException("exception.dentistCalendarLockService.recurrencePattern.date.user", null, "exception.dentistCalendarLockService.recurrencePattern.date.log", new Object[]{dentistCalendarLockRequestCreateDTO.getMode(), dentistCalendarLockRequestCreateDTO.getStartDate(), dentistCalendarLockRequestCreateDTO.getEndDate(), "DentistCalendarLockService", "validateByMode"}, LogLevel.ERROR);
                }
                if (dentistCalendarLockRequestCreateDTO.getRecurrence() == CalendarLockRecurrenceName.NONE) {
                    throw new BadRequestException("exception.dentistCalendarLockService.recurrencePattern.recurrence.user", null, "exception.dentistCalendarLockService.recurrencePattern.recurrence.log", new Object[]{dentistCalendarLockRequestCreateDTO.getMode(), dentistCalendarLockRequestCreateDTO.getRecurrence(), "DentistCalendarLockService", "validateByMode"}, LogLevel.ERROR);
                }
                if (dentistCalendarLockRequestCreateDTO.getDays() == null || dentistCalendarLockRequestCreateDTO.getDays().isEmpty()) {
                    throw new BadRequestException("exception.dentistCalendarLockService.recurrencePattern.days.user", null, "exception.dentistCalendarLockService.recurrencePattern.days.log", new Object[]{dentistCalendarLockRequestCreateDTO.getMode(), "DentistCalendarLockService", "validateByMode"}, LogLevel.ERROR);

                }

            }
        }
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
    public void validateRecurrenceRange(CalendarLockRecurrenceName recurrence, LocalDate startDate, LocalDate endDate) {
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
