package com.odontologiaintegralfm.feature.appointment.core.service.impl;


import com.odontologiaintegralfm.feature.appointment.core.enums.CalendarDayStatus;
import com.odontologiaintegralfm.feature.appointment.core.enums.CalendarHoliday;
import com.odontologiaintegralfm.feature.appointment.core.enums.SlotStatus;
import com.odontologiaintegralfm.feature.appointment.catalogs.model.Holiday;
import com.odontologiaintegralfm.feature.appointment.catalogs.service.IHolidayService;
import com.odontologiaintegralfm.feature.appointment.core.dto.*;
import com.odontologiaintegralfm.feature.appointment.core.enums.AppointmentStatus;
import com.odontologiaintegralfm.feature.appointment.core.model.*;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.*;
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
     * Obtiene el calendario detallado de un día específico para un dentista, incluyendo
     * slots libres, reservados y bloqueados.
     *
     * <p>El método realiza las siguientes operaciones:</p>
     * <ul>
     *     <li>Valida que el dentista exista; en caso contrario, lanza {@link ConflictException}.</li>
     *     <li>Obtiene la disponibilidad configurada del dentista para la fecha indicada.</li>
     *     <li>Si no existe disponibilidad para ese día, devuelve un calendario vacío con información básica.</li>
     *     <li>Verifica si la fecha es feriado y, de serlo, comprueba si el dentista trabaja en dicho feriado.</li>
     *     <li>Genera los slots del día (según jornada habitual o feriado) y los completa con turnos y bloqueos existentes.</li>
     *     <li>Si es feriado y el dentista no trabaja, devuelve un calendario vacío.</li>
     * </ul>
     *
     * @param idDentist ID del dentista del cual se desea obtener el calendario.
     * @param day       Fecha específica a consultar.
     *
     * @return Un {@link Response} que contiene un {@link CalendarDetailDayResponseDTO} con:
     *         <ul>
     *             <li>ID del dentista,</li>
     *             <li>fecha consultada,</li>
     *             <li>lista de {@link SlotResponseDTO} con su estado (FREE, RESERVED, LOCKED)
     *                 e información asociada según corresponda.</li>
     *         </ul>
     *
     * @throws ConflictException Si no existe un dentista con el ID indicado.
     *
     * @see #generateSlot(LocalTime, LocalTime, int) Para la generación de los slots diarios.
     */

    @Override
    public Response<CalendarDetailDayResponseDTO> getCalendarDay(Long idDentist, LocalDate day) {

        //Validar dentista.
        validateDentist(idDentist);
        return new Response<>(true, null, buildCalendarDay(idDentist,day));
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
     * @see #buildCalendarDay(Long, LocalDate) Para construir el detalle diario utilizado en esta generación semanal.
     */

    @Override
    public Response<CalendarWeekResponseDTO> getCalendarWeek(Long idDentist,LocalDate day) {

        validateDentist(idDentist);

        //Obtener la semana calendario a partir del día recibido.
        LocalDate weekStart =  day.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate weekEnd   = day.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));

        List<CalendarDetailDayResponseDTO> days = new ArrayList<>();

        for (LocalDate d = weekStart; !d.isAfter(weekEnd); d = d.plusDays(1)) {
            CalendarDetailDayResponseDTO dayDTO = buildCalendarDay(idDentist, d);
            days.add(dayDTO);
        }

        return new Response<>(true, null, new CalendarWeekResponseDTO(weekStart, weekEnd, days));
    }





    /**
     * Obtiene el calendario mensual de un dentista, generando el estado de cada día del mes solicitado.
     *
     * <p>El método realiza las siguientes operaciones:</p>
     * <ul>
     *     <li>Valida que el dentista exista; de no ser así, lanza {@link ConflictException}.</li>
     *     <li>Determina el rango de fechas del mes utilizando {@link YearMonth}.</li>
     *     <li>Itera desde el día 1 hasta el último día del mes, construyendo el detalle diario mediante {@code buildCalendarDay}.</li>
     *     <li>A partir del detalle diario, deriva el estado global del día (FREE, FULL, LOCKED, NOT_AVAILABLE)
     *         y arma un {@link CalendarGlobalDayDTO} por cada fecha.</li>
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
     *             <li>La lista de {@link CalendarGlobalDayDTO}, uno por cada día del mes,
     *                 con su estado general, descripción y color asociado.</li>
     *         </ul>
     *
     * @throws ConflictException Si no existe un dentista con el ID indicado.
     *
     * @see #buildCalendarDay(Long, LocalDate) Para obtener el detalle completo de cada día.
     * @see CalendarGlobalDayDTO Para el estado global resumido de un día.
     * @see CalendarMonthResponseDTO Para la estructura de la respuesta mensual.
     */

    @Override
    public Response<CalendarMonthResponseDTO> getCalendarMonth(Long idDentist, Integer year, Integer month) {

        //Validar dentista.
        validateDentist(idDentist);

        // Calcular mes
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end   = ym.atEndOfMonth();

        List<CalendarGlobalDayDTO> days = new ArrayList<>();

        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            CalendarDetailDayResponseDTO detail = buildCalendarDay(idDentist, d);

            days.add(new CalendarGlobalDayDTO(
                    d,
                    detail.calendarDayStatus().key(),
                    detail.calendarDayStatus().description(),
                    detail.calendarDayStatus().color()
            ));
        }

        CalendarMonthResponseDTO responseDTO = new CalendarMonthResponseDTO(year, month, days);
        return new Response<>(true, null, responseDTO);
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
     * @param durationSlot   Duración en minutos de cada slot (utilizada para calcular el fin de un turno).
     * @param breakStartTime Inicio del break en la jornada laboral.
     * @param breakEndTime   Fin del break en la jornada laboral.
     * @see SlotStatus Para los diferentes estados posibles de un slot.
     * @see AppointmentResponseDTO Para los datos adjuntos cuando un slot queda reservado.
     * @see DentistCalendarLockResponseDTO Para los datos adjuntos cuando un slot queda bloqueado.
     */

    private void fillSlot(List<SlotResponseDTO> slots, List<Appointment> appointments, List<DentistCalendarLock> locks, Integer durationSlot, LocalTime breakStartTime, LocalTime breakEndTime) {

        // Ordeno appointments del día por hora
        appointments.sort(Comparator.comparing(a -> a.getDate().toLocalTime()));

        // índice para avanzar en appointments sin volver atrás
        int apptIndex = 0;

        for (SlotResponseDTO s : slots) {

            LocalTime slotStart = s.getStartTime();
            LocalTime slotEnd = s.getEndTime();

            //Revisa Breaks
            if(s.getStartTime().isBefore(breakEndTime) && s.getEndTime().isAfter(breakStartTime)) {
                s.setStatus(SlotStatus.BREAK);
                s.setColor(SlotStatus.BREAK.getColorHex());
                s.setAppointment(null);
                s.setCalendarLock(null);
                continue;

            }


            //Revisar bloqueos (no optimizo porque suelen ser pocos)
            for (DentistCalendarLock d : locks) {
                if (d.getStartTime().isBefore(slotEnd) && d.getEndTime().isAfter(slotStart)) {
                    s.setStatus(SlotStatus.LOCKED);
                    s.setColor(SlotStatus.LOCKED.getColorHex());
                    s.setAppointment(null);
                    s.setCalendarLock(DentistCalendarLockResponseDTO.build(d));
                    break;
                }
            }

            if (s.getStatus() == SlotStatus.LOCKED) continue;

            //Revisar turnos
            while (apptIndex < appointments.size()) {

                Appointment a = appointments.get(apptIndex);
                LocalTime apptStart = a.getDate().toLocalTime();

                // si el turno comienza después del fin del slot se corta cortar, porque todo los turnos están ordenados, entonces no hay que seguir iterando.
                if (apptStart.isAfter(slotEnd)) {
                    break;
                }

                // si el turno termina antes de que empiece el slot se salta y avanza al siguiente
                LocalTime apptEnd = apptStart.plusMinutes(durationSlot);
                if (apptEnd.isBefore(slotStart)) {
                    apptIndex++;
                    continue;
                }

                // Si llegamos acá hay solapamiento
                if (!apptStart.isBefore(slotStart) && apptStart.isBefore(slotEnd)) {
                    s.setStatus(SlotStatus.RESERVED);
                    s.setColor(SlotStatus.RESERVED.getColorHex());
                    s.setAppointment(
                            AppointmentResponseDTO.build(a)
                    );
                    break;
                }

                apptIndex++;
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
     * Obtiene el calendario de un día específico para un dentista, incluyendo slots libres, reservados y bloqueados.
     * <p>
     * El método realiza las siguientes validaciones y acciones:
     * <ul>
     *     <li>Obtiene la disponibilidad del dentista para el día especificado.</li>
     *     <li>Si no hay disponibilidad, devuelve un calendario vacío con un mensaje informativo.</li>
     *     <li>Valida si el día es feriado y, en caso que sea, verifica si el dentista trabaja ese feriado.</li>
     *     <li>Genera los slots del día (según horario de disponibilidad o feriado) y los llena con los turnos reservados y bloqueos.</li>
     *     <li>Si el dentista no trabaja en el feriado, se devuelve un calendario vacío.</li>
     *     <li>Para días normales, se obtiene la jornada habitual del dentista y los bloqueos, se generan los slots y se llenan con los turnos y bloqueos correspondientes.</li>
     * </ul>
     *
     * @param idDentist El identificador único del dentista para el cual se consulta el calendario.
     * @param day       La fecha específica del calendario a consultar.
     * @return Un  {@link CalendarDetailDayResponseDTO} con:
     *         <ul>
     *             <li>El ID del dentista.</li>
     *             <li>La fecha consultada.</li>
     *             <li>La lista de {@link SlotResponseDTO} con el estado de cada slot (LIBRE, RESERVADO, BLOQUEADO) y, si corresponde, la información del turno o bloqueo.</li>
     *         </ul>
     * @throws ConflictException Si el dentista con {@code idDentist} no se encuentra en la base de datos.
     *
     * @see #generateSlot(LocalTime, LocalTime, int) Para la generación de slots según horario y duración.
     */
    private CalendarDetailDayResponseDTO buildCalendarDay(Long idDentist, LocalDate day){

        DentistAvailability dentistAvailability = dentistAvailabilityService.getDentistAvailabilityByDate(idDentist,day);
        if (dentistAvailability == null) {

            return new CalendarDetailDayResponseDTO(
                    idDentist,
                    day,
                    CalendarDayStatusResponseDTO.build(CalendarDayStatus.NOT_AVAILABLE),
                    null,
                    Collections.emptyList()
            );

        }


        //1. Validar si es feriado.
        Optional<Holiday> holiday = holidayService.getByDate(day);

        //Si es feriado, valída que lo trabaje el dentista.
        if(holiday.isPresent()) {
            Optional <DentistHoliday> dentistHoliday = dentistHolidayService.getByDentistIdAndHolidayId(idDentist, holiday.get().getId());
            if(dentistHoliday.isPresent()) {

                //Recupera horarios de inicio/fin del feriado, junto con duración del turno.
                LocalTime startTime = dentistHoliday.get().getStartTime();
                LocalTime endTime = dentistHoliday.get().getEndTime();
                int durationSlot = dentistAvailabilityService.getAppointmentDuration(idDentist);

                //Generar slots vacíos.
                List<SlotResponseDTO> slots = generateSlot(startTime, endTime, durationSlot);

                //Buscar turnos del día
                List<Appointment> appointments = appointmentService.getAppointmentByDentistAndDate(idDentist, day, AppointmentStatus.RESERVED);

                //Llenar slots.
                fillSlot(slots,appointments, Collections.emptyList(),dentistAvailability.getAppointmentDuration(),dentistAvailability.getBreakStartTime(),dentistAvailability.getEndTime());

                return new CalendarDetailDayResponseDTO(
                        idDentist,
                        day,
                        deriveDayStatus(slots),
                        CalendarHolidayResponseDTO.build(CalendarHoliday.HOLIDAY,holiday.get().getName(),holiday.get().getType().getLabel()),
                        slots);

            }else{
                return new  CalendarDetailDayResponseDTO(
                        idDentist,
                        day,
                        CalendarDayStatusResponseDTO.build(CalendarDayStatus.NOT_AVAILABLE),
                        CalendarHolidayResponseDTO.build(CalendarHoliday.HOLIDAY,holiday.get().getName(),holiday.get().getType().getLabel()),
                        Collections.emptyList());
            }
        }



        //2. Valida jornada de trabajo habitual.
        List<DentistCalendarLock> dentistCalendarLocks = dentistCalendarLockService.getByDate(idDentist,day);

        //Generar slots vacíos.
        List<SlotResponseDTO> slots = generateSlot(dentistAvailability.getStartTime(), dentistAvailability.getEndTime(), dentistAvailability.getAppointmentDuration());

        //Buscar turnos del día
        List<Appointment> appointments = appointmentService.getAppointmentByDentistAndDate(idDentist, day, AppointmentStatus.RESERVED);

        //Llenar slots.
        fillSlot(slots,appointments, dentistCalendarLocks, dentistAvailability.getAppointmentDuration(),dentistAvailability.getBreakStartTime(),dentistAvailability.getBreakEndTime() );

        return new CalendarDetailDayResponseDTO(idDentist, day,deriveDayStatus(slots),null,slots);

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

    private CalendarDayStatusResponseDTO deriveDayStatus(List<SlotResponseDTO> slots) {

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
