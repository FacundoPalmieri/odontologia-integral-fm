package com.odontologiaintegralfm.feature.appointmentscheduling.calendar.service;


import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.dto.AppointmentResponseDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.model.Appointment;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.service.IAppointmentService;
import com.odontologiaintegralfm.feature.appointmentscheduling.calendar.dto.*;
import com.odontologiaintegralfm.feature.appointmentscheduling.locktype.enums.CalendarLockMode;
import com.odontologiaintegralfm.feature.appointmentscheduling.calendar.enums.CalendarDayStatus;
import com.odontologiaintegralfm.feature.appointmentscheduling.calendar.enums.CalendarHoliday;
import com.odontologiaintegralfm.feature.appointmentscheduling.calendar.enums.SlotStatus;
import com.odontologiaintegralfm.feature.appointmentscheduling.holiday.model.Holiday;
import com.odontologiaintegralfm.feature.appointmentscheduling.holiday.service.IHolidayService;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.enums.AppointmentStatus;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistavailability.service.IDentistAvailabilityService;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistavailability.model.DentistAvailability;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistholiday.model.DentistHoliday;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistholiday.service.IDentistHolidayService;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.dto.DentistCalendarLockResponseDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.model.DentistCalendarLock;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.service.IDentistCalendarLockService;
import com.odontologiaintegralfm.feature.dentist.core.model.Dentist;
import com.odontologiaintegralfm.feature.dentist.core.service.interfaces.IDentistService;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.shared.dto.Response;
import org.springframework.stereotype.Service;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.*;


/**
 * Servicio principal encargado de construir, procesar y exponer la información del
 * calendario operativo de un dentista. Centraliza la lógica relacionada con:
 *
 * <ul>
 *     <li><strong>Disponibilidad del dentista</strong> (jornadas habituales, horarios especiales de feriados).</li>
 *     <li><strong>Turnos reservados</strong> y su impacto en los slots del día.</li>
 *     <li><strong>Bloqueos de agenda</strong> (manuales o con recurrencia) aplicados al calendario.</li>
 *     <li><strong>Feriados</strong> y configuración específica de trabajo en feriados.</li>
 * </ul>
 *
 * El servicio expone tres niveles de consulta:
 *
 * <h3>1. Calendario diario</h3>
 * Genera la grilla completa de slots de un día, determinando para cada slot si está
 * <em>libre</em>, <em>reservado</em> o <em>bloqueado</em>, adjuntando información del turno o
 * bloqueo según corresponda.
 *
 * <h3>2. Calendario semanal</h3>
 * Construye los calendarios diarios desde lunes hasta domingo de la semana que contiene
 * la fecha solicitada.
 *
 * <h3>3. Calendario mensual</h3>
 * Evalúa cada día del mes y deriva un estado global (FREE, FULL, LOCKED, NOT_AVAILABLE)
 * basado en el detalle diario generado dinámicamente.
 *
 * <h3>Funciones destacadas</h3>
 * <ul>
 *     <li>Validación de existencia del dentista.</li>
 *     <li>Obtención de disponibilidad habitual y especial por feriados.</li>
 *     <li>Construcción dinámica de slots según duración configurada.</li>
 *     <li>Identificación de solapamientos con turnos o bloqueos.</li>
 *     <li>Optimización en el procesamiento de turnos mediante ordenamiento e indexado.</li>
 * </ul>
 *
 * Este servicio actúa como capa de orquestación entre:
 * <ul>
 *     <li>{@link IDentistAvailabilityService}</li>
 *     <li>{@link IAppointmentService}</li>
 *     <li>{@link IDentistCalendarLockService}</li>
 *     <li>{@link IHolidayService} y {@link IDentistHolidayService}</li>
 * </ul>
 *
 * Su propósito es entregar una visión coherente, eficiente y completa del estado de la agenda
 * del dentista en cualquier rango de fechas.
 */

@Service
public class CalendarService implements ICalendarService {

    private final IHolidayService holidayService;
    private final IDentistHolidayService dentistHolidayService;
    private final IDentistService dentistService;
    private final IDentistAvailabilityService dentistAvailabilityService;
    private final IAppointmentService appointmentService;
    private final IDentistCalendarLockService dentistCalendarLockService;

    public CalendarService(
            IHolidayService holidayService,
            IDentistHolidayService dentistHolidayService,
            IDentistService dentistService,
            IDentistAvailabilityService dentistAvailabilityService,
            IAppointmentService appointmentService,
            IDentistCalendarLockService dentistCalendarLockService
    ) {
        this.holidayService = holidayService;
        this.dentistHolidayService = dentistHolidayService;
        this.dentistService = dentistService;
        this.dentistAvailabilityService = dentistAvailabilityService;
        this.appointmentService = appointmentService;
        this.dentistCalendarLockService = dentistCalendarLockService;
    }




    /**
     * Obtiene el calendario detallado de un día específico para un dentista.
     *
     * <p>
     * Este método actúa como punto de entrada del flujo de construcción del calendario diario.
     * Se encarga exclusivamente de:
     * </p>
     * <ul>
     *   <li>Validar la existencia del dentista.</li>
     *   <li>Verificar que el dentista tenga al menos una disponibilidad configurada.</li>
     *   <li>Recuperar todos los datos necesarios para la construcción del día:
     *       <ul>
     *         <li>Disponibilidad efectiva del dentista para la fecha.</li>
     *         <li>Bloqueos de agenda que apliquen a la fecha.</li>
     *         <li>Feriado (si existe).</li>
     *         <li>Turnos reservados del día.</li>
     *       </ul>
     *   </li>
     * </ul>
     *
     * <p>
     * La lógica de negocio y la resolución de escenarios (feriados, vacaciones,
     * días no laborables, generación de slots y estados del día) se delega al método
     * {@link #buildDay(Long, LocalDate, DentistAvailability, List, Optional, List)}.
     * </p>
     *
     * @param idDentist Identificador único del dentista.
     * @param day       Fecha específica a consultar.
     *
     * @return Un {@link Response} que contiene un {@link CalendarDayResponseDTO}
     *         con el detalle completo del calendario diario.
     *
     * @throws ConflictException Si el dentista no existe o no posee ninguna disponibilidad configurada.
     *
     * @see #buildDay(Long, LocalDate, DentistAvailability, List, Optional, List)
     */


    @Override
    public Response<CalendarDayResponseDTO> getCalendarDay(Long idDentist, LocalDate day) {

        //Validar dentista.
        validateDentist(idDentist);

        //Verifica que al menos exista una disponibilidad(puede tener disponibilidad en otro día)
        List<DentistAvailability> dentistAvailabilities = dentistAvailabilityService.getByIdInternal(idDentist);

        if (dentistAvailabilities.isEmpty()) {
            throw new ConflictException("exception.calendarService.user", null, "exception.calendarService.log", new Object[]{idDentist, "CalendarService", "getCalendarDay"}, LogLevel.ERROR);
        }


        //se obtiene la jornada para ese día puntual
        DentistAvailability dentistAvailability = dentistAvailabilityService.getDentistAvailabilityByDate(idDentist,day,dentistAvailabilities);

        //Se recuperan bloqueos si existen
        List<DentistCalendarLock> dentistCalendarLocks = dentistCalendarLockService.getByDate(idDentist,day);

        //Validar si es feriado.
        Optional<Holiday> holidayOptional = holidayService.getByDate(day);

        //Buscar turnos del día
        List<Appointment> appointments = appointmentService.getAppointmentByDentistAndDate(idDentist, day, AppointmentStatus.RESERVED);

        //Se llama a construir día y se retorna respuesta.
        return new Response<>(true, null, buildDay(idDentist,day, dentistAvailability,dentistCalendarLocks,holidayOptional, appointments));
    }



    /**
     * Obtiene el calendario semanal completo para un dentista, a partir de una fecha específica.
     *
     * <p>La semana se calcula tomando como inicio el lunes y como fin el domingo de la semana a la que
     * pertenece la fecha proporcionada. Para cada día dentro de ese rango, se construye el
     * calendario diario correspondiente.</p>
     *
     * <ul>
     *     <li>Valida que el dentista exista; de no ser así, lanza {@link ConflictException}.</li>
     *     <li>Determina el inicio ({@link DayOfWeek#MONDAY}) y fin ({@link DayOfWeek#SUNDAY}) de la semana.</li>
     *     <li>Itera desde el lunes hasta el domingo, generando el calendario detallado de cada día mediante {@code buildCalendarDay}.</li>
     *     <li>Retorna un objeto que encapsula los datos de toda la semana.</li>
     * </ul>
     *
     * @param idDentist ID del dentista cuyo calendario semanal se desea consultar.
     * @param day       Fecha de referencia dentro de la semana a obtener.
     *
     * @return Un {@link Response} que contiene un {@link CalendarWeekResponseDTO} con:
     *         <ul>
     *             <li>La fecha de inicio (lunes) y fin (domingo) de la semana.</li>
     *             <li>La lista de calendarios diarios generados para cada día del rango.</li>
     *         </ul>
     *
     * @throws ConflictException Si no existe un dentista con el ID indicado.
     *
     * @see #getCalendarDay(Long, LocalDate) Para consultar el calendario de un día específico.
     * @see #buildDay(Long, LocalDate, DentistAvailability, List, Optional, List) Para construir el detalle diario utilizado en esta generación semanal.
     */

    @Override
    public Response<CalendarWeekResponseDTO> getCalendarWeek(Long idDentist,LocalDate day) {

        //Validar dentista.
        validateDentist(idDentist);

        //Verifica que al menos exista una disponibilidad.
        List<DentistAvailability> dentistAvailabilities = dentistAvailabilityService.getByIdInternal(idDentist);


        //Obtiene la semana calendario a partir del día recibido.
        LocalDate start =  day.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate end   = day.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));


        //Construye la vista.
        List<CalendarDayResponseDTO> days = buildCalendarDays(idDentist,dentistAvailabilities,start,end);

        return new Response<>(true, null, new CalendarWeekResponseDTO(start, end, days));
    }




    /**
     * Obtiene el calendario mensual de un dentista, generando el estado de cada día del mes solicitado.
     *
     * <p>El método realiza las siguientes operaciones:</p>
     * <ul>
     *     <li>Valida que el dentista exista; de no ser así, lanza {@link ConflictException}.</li>
     *     <li>Determina el rango de fechas del mes utilizando {@link YearMonth}.</li>
     *     <li>Itera desde el día 1 hasta el último día del mes, construyendo el detalle diario mediante {@code buildCalendarDay}.</li>
     *     <li>Devuelve un objeto que contiene el año, mes y todos los días con su estado resumido.</li>
     * </ul>
     *
     * @param idDentist ID del dentista cuyo calendario mensual se desea consultar.
     * @param year      Año del calendario solicitado.
     * @param month     Mes del calendario solicitado (1–12).
     *
     * @return Un {@link Response} que contiene un {@link CalendarMonthResponseDTO} con:
     *         <ul>
     *             <li>El año y mes solicitados.</li>
     *             <li>La lista de {@link CalendarDayResponseDTO}, uno por cada día del mes,
     *                 con su estado general, descripción y color asociado.</li>
     *         </ul>
     *
     * @throws ConflictException Si no existe un dentista con el ID indicado.
     *
     * @see #buildDay(Long, LocalDate, DentistAvailability, List, Optional, List) Para obtener el detalle completo de cada día.
     * @see CalendarMonthResponseDTO Para la estructura de la respuesta mensual.
     */

    @Override
    public Response<CalendarMonthResponseDTO> getCalendarMonth(Long idDentist, Integer year, Integer month) {

        //Validar dentista.
        validateDentist(idDentist);

        //Verifica que al menos exista una disponibilidad.
        List<DentistAvailability> dentistAvailabilities = dentistAvailabilityService.getByIdInternal(idDentist);

        // Calcular mes
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end   = ym.atEndOfMonth();

        //Construye la vista.
        List<CalendarDayResponseDTO> days = buildCalendarDays(idDentist,dentistAvailabilities,start,end);

        //Quita el detalle de los slot.
        days.stream()
                .forEach(
                        day -> day.setSlots(Collections.emptyList())
                );


        CalendarMonthResponseDTO responseDTO = new CalendarMonthResponseDTO(year, month, days);
        return new Response<>(true, null, responseDTO);
    }




    /**
     * Construye la vista de calendario para un rango de fechas determinado
     * generando un {@link CalendarDayResponseDTO} por cada día comprendido
     * entre {@code start} y {@code end}.
     *
     * Se utiliza en Vista Semanal y Mensual (Lógica compartida)
     *
     * <p>
     * Este método centraliza la lógica de armado del calendario y es utilizado por la vista semanal y mensual.
     * </p>
     *
     * <p>
     * Para optimizar performance, los datos necesarios se obtienen previamente
     * en forma agrupada:
     * </p>
     * <ul>
     *     <li>Bloqueos del dentista, agrupados por fecha</li>
     *     <li>Feriados del rango</li>
     *     <li>Turnos del rango</li>
     * </ul>
     *
     * <p>
     * Luego, por cada día del rango:
     * </p>
     * <ul>
     *     <li>Se obtiene la disponibilidad correspondiente</li>
     *     <li>Se filtran los bloqueos aplicables al día</li>
     *     <li>Se determina si el día es feriado</li>
     *     <li>Se asocian los turnos del día</li>
     *     <li>Se construye el {@link CalendarDayResponseDTO}</li>
     * </ul>
     *
     * @param idDentist             identificador del dentista
     * @param dentistAvailabilities lista de disponibilidades del dentista
     * @param start                fecha inicial del rango (inclusive)
     * @param end                  fecha final del rango (inclusive)
     *
     * @return lista de {@link CalendarDayResponseDTO} representando el calendario día por día dentro del rango indicado
     */

    private  List<CalendarDayResponseDTO> buildCalendarDays(Long idDentist,List<DentistAvailability> dentistAvailabilities, LocalDate start, LocalDate end){

        //Obtiene bloqueos. Se obtiene un map para bloqueos del mes.
        Map<LocalDate, List<DentistCalendarLock>> locksByDate = dentistCalendarLockService.getByDateRange(idDentist,start,end);


        //Obtiene feriados para el mes
        Map<LocalDate, Holiday> holidaysByDate = holidayService.getByDateRange(start, end);


        //Obtener turnos para el mes
        Map<LocalDate, List<Appointment>> appointmentsByDate = appointmentService.getByDateRange(idDentist,start.atStartOfDay(),end.atTime(LocalTime.MAX),AppointmentStatus.RESERVED);


        List<CalendarDayResponseDTO> days = new ArrayList<>();

        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            //se obtiene la jornada para el día de iteración.
            DentistAvailability dentistAvailability = dentistAvailabilityService.getDentistAvailabilityByDate(idDentist,d,dentistAvailabilities);

            //Se obtiene bloqueo solo del día de iteración
            List<DentistCalendarLock> dayLocks = locksByDate.getOrDefault(d, Collections.emptyList());

            //Obtiene feriado para el día de la iteración.
            Optional<Holiday> holiday = Optional.ofNullable(holidaysByDate.get(d));

            //Obtiene turnos para el día de iteración.
            List<Appointment> appointments = appointmentsByDate.getOrDefault(d, Collections.emptyList());

            CalendarDayResponseDTO dayDTO = buildDay(idDentist, d,dentistAvailability ,dayLocks, holiday,appointments );
            days.add(dayDTO);
        }

        return days;
    }




    /**
     * Completa una lista de slots con información de turnos reservados y bloqueos
     * del dentista, asignando el estado correspondiente (FREE, RESERVED o LOCKED) a cada slot.
     *
     * <p>El método aplica la siguiente lógica:</p>
     * <ul>
     *     <li>Los <strong>bloqueos</strong> se revisan primero. Si un slot se solapa con un bloqueo,
     *         se marca como {@code LOCKED} y se adjunta la información del bloqueo.</li>
     *     <li>Si el slot no está bloqueado, se revisan los <strong>turnos</strong> del día
     *         (previamente ordenados por hora).</li>
     *     <li>Se utiliza un índice  para avanzar en la lista de turnos sin retroceder,
     *         optimizando el procesamiento.</li>
     *     <li>Si un turno se solapa con el rango horario del slot,
     *         el slot se marca como {@code RESERVED} y se adjunta la información del turno.</li>
     *     <li>Si un slot no coincide con ningún bloqueo ni turno, se marca como {@code FREE}.</li>
     * </ul>
     *
     * <h4>Reglas de solapamiento aplicadas</h4>
     * <ul>
     * Regla de solapamiento entre dos intervalos de tiempo A y B:
     *
     * Dos intervalos A y B se solapan si:
     *
     * 1) El inicio de A es anterior al fin de B
     *    (A.start < B.end)
     * Y
     * 2) El fin de A es posterior al inicio de B
     *    (A.end > B.start)

     * "Dos cosas se pisan si ninguna termina antes de que la otra empiece".
     *
     * Ejemplo visual:
     *
     * Tiempo - >
     *
     * A: |--------|
     * B:       |--------|
     *
     * En este caso:
     * - A.start < B.end    -> verdadero
     * - A.end   > B.start  -> verdadero
     *
     * Por lo tanto, A y B se solapan.
     *
     * Casos que NO se solapan:
     *
     * A: |--------|
     * B:           |--------|
     * (B empieza después de que A termina)
     *
     * A:           |--------|
     * B: |--------|
     * (B termina antes de que A empiece)
     *
     *
     * El inicio de uno es anterior al fin del otro y El fin de uno es posterior al inicio del otro
     * </ul>
     *
     * @param slots          Lista de slots generados para el día, a ser completados.
     * @param appointments   Lista de turnos del día (se espera que pertenezcan todos a la misma fecha).
     * @param locks          Lista de bloqueos configurados para el dentista en ese día.
     * @param breakStartTime Inicio del break en la jornada laboral.
     * @param breakEndTime   Fin del break en la jornada laboral.
     * @see SlotStatus Para los diferentes estados posibles de un slot.
     * @see AppointmentResponseDTO Para los datos adjuntos cuando un slot queda reservado.
     * @see DentistCalendarLockResponseDTO Para los datos adjuntos cuando un slot queda bloqueado.
     */

    private void fillSlot(List<SlotResponseDTO> slots, List<Appointment> appointments, List<DentistCalendarLock> locks, LocalTime breakStartTime, LocalTime breakEndTime) {

        // Ordeno appointments del día por hora
        appointments.sort(Comparator.comparing(a -> a.getDate().toLocalTime()));
        locks.sort(Comparator.comparing(DentistCalendarLock::getStartTime));


        // índice para avanzar en appointments sin volver atrás
        int apptIndex = 0;
        int lockIndex = 0;

        for (SlotResponseDTO s : slots) {

            LocalTime slotStart = s.getStartTime();
            LocalTime slotEnd = s.getEndTime();

            //Revisa Breaks, siempre que exista
            if(breakStartTime != null || breakEndTime != null) {
                if(s.getStartTime().isBefore(breakEndTime) && s.getEndTime().isAfter(breakStartTime)) {
                    s.setStatus(SlotStatus.BREAK);
                    s.setColor(SlotStatus.BREAK.getColorHex());
                    s.setAppointment(null);
                    s.setCalendarLock(null);
                    continue;

                }
            }



            //Revisar bloqueos
            while (lockIndex < locks.size()) {

                DentistCalendarLock lock = locks.get(lockIndex);

                if(lock.isFullDay()){
                    s.setStatus(SlotStatus.LOCKED);
                    s.setColor(SlotStatus.LOCKED.getColorHex());
                    s.setCalendarLock(DentistCalendarLockResponseDTO.build(lock,null));
                    s.setAppointment(null);
                    break;
                }

                // Si el bloqueo terminó antes del slot -> avanzar
                if (lock.getEndTime().isBefore(slotStart)) {
                    lockIndex++;
                    continue;
                }

                // Si el bloqueo empieza después del slot -> no hay solapamiento
                if (lock.getStartTime().isAfter(slotEnd)) {
                    break;
                }

                // Hay solapamiento
                s.setStatus(SlotStatus.LOCKED);
                s.setColor(SlotStatus.LOCKED.getColorHex());
                s.setCalendarLock(DentistCalendarLockResponseDTO.build(lock,null));
                s.setAppointment(null);
                break;
            }

            if (s.getStatus() == SlotStatus.LOCKED) {
                continue;
            }

            //Revisar turnos
            while (apptIndex < appointments.size()) {

                Appointment a = appointments.get(apptIndex);
                LocalTime apptStart = a.getDate().toLocalTime();

                // si el turno no es anterior fin del slot se corta cortar, porque todo los turnos están ordenados, entonces no hay que seguir iterando.
                if (!apptStart.isBefore(slotEnd)) {
                    break;
                }

                // si el turno es anterior de que empiece el slot se salta y avanza al siguiente
                if (apptStart.isBefore(slotStart)) {
                    apptIndex++;
                    continue;
                }

                // Si llegamos acá hay solapamiento
                    s.setStatus(SlotStatus.RESERVED);
                    s.setColor(SlotStatus.RESERVED.getColorHex());
                    s.setAppointment(
                            AppointmentResponseDTO.build(a)
                    );
                    break;
            }

            if (s.getStatus() == null) {
                s.setStatus(SlotStatus.FREE);
                s.setColor(SlotStatus.FREE.getColorHex());
            }
        }
    }






    /**
     * Genera una lista de slots vacíos con horarios consecutivos entre dos horas dadas,
     * utilizando una duración fija por slot.
     *
     * <p>El método comienza en {@code startTime} y crea intervalos consecutivos de
     * {@code durationSlot} minutos hasta llegar a {@code endTime}.
     * Solo se generan los slots cuyo final no excede la hora de fin.</p>
     *
     * <ul>
     *     <li>Cada slot se crea inicialmente con estado {@code FREE}.</li>
     *     <li>Los slots son contiguos: el inicio de uno coincide con el fin del anterior.</li>
     *     <li>No se genera un slot si su {@code slotEnd} queda después de {@code endTime}.</li>
     * </ul>
     *
     * @param startTime     Hora de inicio del rango a generar.
     * @param endTime       Hora límite para la generación de slots. El último slot no puede finalizar después de este horario.
     * @param durationSlot  Duración de cada slot en minutos.
     *
     * @return Una lista de {@link SlotResponseDTO} representando los intervalos generados,
     *         cada uno con inicio, fin y estado inicial {@code FREE}.
     *
     * @see SlotResponseDTO Para la estructura de un slot generado.
     * @see SlotStatus      Para el estado inicial de los slots.
     */

    private List<SlotResponseDTO> generateSlot(LocalTime startTime, LocalTime endTime, int durationSlot) {
        List<SlotResponseDTO> list = new ArrayList<>();
        LocalTime slotStart = startTime;

        while (!slotStart.plusMinutes(durationSlot).isAfter(endTime)) {
            LocalTime slotEnd = slotStart.plusMinutes(durationSlot);
            list.add(new SlotResponseDTO(slotStart,slotEnd , SlotStatus.FREE,SlotStatus.FREE.getColorHex(), null, null));
            slotStart = slotStart.plusMinutes(durationSlot);
        }
        return list;


    }





    /**
     * Construye el calendario diario de un dentista resolviendo todos los escenarios
     * de negocio posibles para una fecha determinada.
     *
     * <p>
     * Este método centraliza la lógica de decisión del calendario diario y evalúa,
     * en orden de precedencia:
     * </p>
     *
     * <ol>
     *   <li><b>Feriados</b>: determina si la fecha es feriado y si el dentista trabaja o no.</li>
     *
     *   <li><b>Bloqueos de día completo</b> (vacaciones, licencias):
     *       si existe un bloqueo continuo, el día se marca como {@code LOCKED} independientemente de la disponibilidad habitual.</li>
     *
     *   <li><b>Disponibilidad habitual</b>: si no existe jornada configurada para ese día se considera {@code NOT_AVAILABLE}.</li>
     *
     *
     *   <li><b>Generación de slots</b>: se crean los slots horarios según la jornada (habitual o feriado).</li>
     *
     *   <li><b>Ocupación de slots</b>: los slots se completan con turnos, bloqueos parciales y breaks.</li>
     * </ol>
     *
     * <p>
     * La generación de slots se delega a {@link #generateSlot(LocalTime, LocalTime, int)}
     * y su posterior resolución a {@link #fillSlot(List, List, List, LocalTime, LocalTime)}.
     * El estado global del día se calcula mediante
     * {@link #deriveDayStatus(List, List, List)}.
     * </p>
     *
     * @param idDentist            Identificador del dentista.
     * @param date                 Fecha del calendario a construir.
     * @param dentistAvailability Disponibilidad efectiva del dentista para la fecha,
     *                             o {@code null} si no trabaja ese día.
     * @param dentistCalendarLocks Bloqueos que aplican a la fecha.
     * @param holidayOptional      Feriado correspondiente a la fecha, si existe.
     * @param appointments         Turnos reservados del día.
     *
     * @return Un {@link CalendarDayResponseDTO} con el estado del día y sus slots,
     *         o sin slots si el día no es laborable o está completamente bloqueado.
     *
     * @see #generateSlot(LocalTime, LocalTime, int)
     * @see #fillSlot(List, List, List, LocalTime, LocalTime)
     * @see #deriveDayStatus(List, List, List)
     */

    private CalendarDayResponseDTO buildDay(Long idDentist, LocalDate date, DentistAvailability dentistAvailability, List<DentistCalendarLock> dentistCalendarLocks , Optional<Holiday> holidayOptional, List<Appointment> appointments){

        //1. Validar si es feriado.

        //Si es feriado, valída que lo trabaje el dentista.
        if(holidayOptional.isPresent()) {

            Holiday holiday = holidayOptional.get();

            Optional <DentistHoliday> dentistHolidayOptional = dentistHolidayService.getByDentistIdAndHolidayId(idDentist, holiday.getId());
            if(dentistHolidayOptional.isPresent()) {

                DentistHoliday dentistHoliday = dentistHolidayOptional.get();

                //Recupera horarios de inicio/fin del feriado, junto con duración del turno.
                LocalTime startTime = dentistHoliday.getStartTime();
                LocalTime endTime = dentistHoliday.getEndTime();
                int durationSlot = dentistHoliday.getAppointmentDuration();

                //Generar slots vacíos.
                List<SlotResponseDTO> slots = generateSlot(startTime, endTime, durationSlot);


                //Llenar slots.
                fillSlot(slots,appointments, Collections.emptyList(),dentistHoliday.getBreakStartTime(),dentistHoliday.getBreakEndTime());

                return  CalendarDayResponseDTO.build(
                        idDentist,
                        dentistHoliday.getId(),
                        date,
                        deriveDayStatus(slots, Collections.emptyList(), Collections.emptyList()), //Arma el estado general del día
                        CalendarHolidayResponseDTO.build(holiday.getId(), CalendarHoliday.HOLIDAY,holiday.getName(),holiday.getType().getLabel()),
                        Collections.emptyList(),
                        slots
                );

            }else{
                return  CalendarDayResponseDTO.build(
                        idDentist,
                        null,
                        date,
                        CalendarDayStatusResponseDTO.build(CalendarDayStatus.NOT_AVAILABLE),
                        CalendarHolidayResponseDTO.build(holiday.getId(),CalendarHoliday.HOLIDAY,holiday.getName(),holiday.getType().getLabel()),
                        Collections.emptyList(),
                        Collections.emptyList()
                );
            }
        }



        //2. Valida jornada de trabajo habitual.

        //A. Valída si está de vacaciones o de licencia por enfermedad.

        //Lista de bloqueos para el encabezado del día. Se completa y se devuelve en la respuesta. Es independiente de los slots.
        List<CalendarDentistLock> calendarLocks = new ArrayList<>(List.of());

        for(DentistCalendarLock dcl : dentistCalendarLocks){
            if(dcl.getType().getModes().contains(CalendarLockMode.DAILY_CONTINUOUS) || dcl.isFullDay()){
                CalendarDentistLock calendarDentistLock = new CalendarDentistLock(
                        dcl.getId(),
                        dcl.getType().getName()
                );

                calendarLocks.add(calendarDentistLock);

                //Si está de vacaciones, licencia o el bloque es fullDay se retorna respuesta. No hay que evaluar más.
               return CalendarDayResponseDTO.build(
                        idDentist,
                        idDentist,
                        date,
                        CalendarDayStatusResponseDTO.build(CalendarDayStatus.LOCKED),
                        null,
                        calendarLocks,
                        Collections.emptyList()
                );
            }
        }



        //B. Si no hay jornada para ese día, se devuelve como NO DISPONIBLE.
        if(dentistAvailability == null) {
            return  CalendarDayResponseDTO.build(idDentist,null, date,CalendarDayStatusResponseDTO.build(CalendarDayStatus.NOT_AVAILABLE),null,null,null);
        }




        //C. Sabemos que ese día trabaja y no tiene bloqueos.

        //Generar slots vacíos.
        List<SlotResponseDTO> slots = generateSlot(dentistAvailability.getStartTime(), dentistAvailability.getEndTime(), dentistAvailability.getAppointmentDuration());


        //Llenar slots.
        fillSlot(slots,appointments, dentistCalendarLocks,dentistAvailability.getBreakStartTime(),dentistAvailability.getBreakEndTime());

        return  CalendarDayResponseDTO.build(
                idDentist,
                null,
                date,
                deriveDayStatus(slots,dentistCalendarLocks,calendarLocks), // Se pasa dentistLockList para que sea llenada dentro del método y utilizado en este mismo return.
                null,
                calendarLocks,
                slots);

    }



    /**
     * Determina el estado global de un día en función de la lista de slots generados.
     *
     * <p>El estado del día se calcula según las siguientes reglas:</p>
     * <ul>
     *     <li>Si no hay slots, el día se considera {@code NOT_AVAILABLE}.</li>
     *     <li>Si al menos un slot está libre ({@code FREE}), el día se marca como {@code FREE}.</li>
     *     <li>Si todos los slots están bloqueados ({@code LOCKED}), el día se marca como {@code LOCKED}.</li>
     *     <li>En cualquier otro caso (por ejemplo, todos reservados o combinación de reservados/bloqueados),
     *         el día se marca como {@code FULL}.</li>
     * </ul>
     *
     * @param slots Lista de slots del día cuya información se utiliza para evaluar el estado global.
     *
     * @return Un {@link CalendarDayStatus} representando el estado general del día.
     *
     * @see SlotStatus Para los posibles estados individuales de un slot.
     * @see CalendarDayStatus Para los estados globales posibles del día.
     */

    private CalendarDayStatusResponseDTO deriveDayStatus(List<SlotResponseDTO> slots, List<DentistCalendarLock> dentistCalendarLock, List<CalendarDentistLock> calendarLocks) {

        if (slots.isEmpty()) {
            return CalendarDayStatusResponseDTO.build(CalendarDayStatus.NOT_AVAILABLE);
        }



        boolean anyFree   = slots.stream().anyMatch(s -> s.getStatus() == SlotStatus.FREE);
        boolean allLocked = slots.stream().allMatch(s -> s.getStatus() == SlotStatus.LOCKED);

        if (anyFree)   return   CalendarDayStatusResponseDTO.build(CalendarDayStatus.FREE);
        if (allLocked) return   CalendarDayStatusResponseDTO.build(CalendarDayStatus.LOCKED);
        return   CalendarDayStatusResponseDTO.build(CalendarDayStatus.FULL);
    }




    private void validateDentist(Long idDentist){
        Dentist dentist = dentistService.getById(idDentist)
                .orElseThrow(() -> new ConflictException("exception.dentistNotFound.user", null, "exception.dentistNotFound.log", new Object[]{idDentist, "Calendar Service", "getCalendarDay"}, LogLevel.ERROR));

    }

}
