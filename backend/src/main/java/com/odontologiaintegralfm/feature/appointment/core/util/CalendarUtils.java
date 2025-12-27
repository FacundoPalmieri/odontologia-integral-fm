package com.odontologiaintegralfm.feature.appointment.core.util;


import com.odontologiaintegralfm.feature.appointment.core.enums.CalendarLockRecurrenceName;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public final class CalendarUtils {

    private CalendarUtils() {}

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
    public static LocalDate findFirstMatchingDate(LocalDate startDate, DayOfWeek daysBlock) {

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
    public static LocalDate findLastMatchingDate(LocalDate endDate, DayOfWeek daysBlock) {

        // Buscar el último día (en los próximos 7) que coincida con alguno de los días activos
        for (int i = 0; i < 7; i++) {
            LocalDate candidate = endDate.minusDays(i);
            if (daysBlock.equals(candidate.getDayOfWeek())) {
                return candidate;
            }
        }
        return endDate;
    }




    /**
     * Verifica si una fecha/hora específica tiene solapamiento con un evento definido por rango de fechas, días válidos y horario.
     * <p>
     * La verificación considera la fecha y hora del turno, los días de la semana válidos para el evento,
     * el horario de inicio y fin, y la recurrencia del bloqueo (por ejemplo, NONE, DAILY, WEEKLY, etc.).
     * </p>
     *
     * @param dateTime        Fecha y hora del turno a evaluar.
     * @param eventStartDate  Fecha de inicio del evento o bloqueo.
     * @param eventEndDate    Fecha de fin del evento o bloqueo.
     * @param eventDay        Lista de {@link DayOfWeek} que representan los días válidos del evento/bloqueo.
     * @param eventStartTime  Hora de inicio del evento/bloqueo.
     * @param eventEndTime    Hora de fin del evento/bloqueo.
     * @param eventRecurrence Tipo de recurrencia del evento/bloqueo ({@link CalendarLockRecurrenceName}).
     * @return true si el turno cae dentro del evento o bloqueo según fecha, día, recurrencia y horario; false en caso contrario.
     */

    public static boolean isDateTimeWithinEvent(
            LocalDateTime dateTime,
            LocalDate eventStartDate,
            LocalDate eventEndDate,
            DayOfWeek eventDay,
            LocalTime eventStartTime,
            LocalTime eventEndTime,
            CalendarLockRecurrenceName eventRecurrence
    ) {
        LocalDate appointmentDate = dateTime.toLocalDate();
        LocalTime appointmentTime = dateTime.toLocalTime();


        // Verificar rango de fechas para jornada de evento específicas.
        if (eventDay == null && eventRecurrence == null) {
            if (appointmentDate.isBefore(eventStartDate) || appointmentDate.isAfter(eventEndDate)) {
                return false;
            }
        }


        // Verificar día + recurrencia combinados
        if(eventDay != null){
            DayOfWeek appointmentDay = appointmentDate.getDayOfWeek();

            boolean isValidDayAndRecurrence =
                    eventDay.equals(appointmentDay)
                            && eventRecurrence.matches(eventStartDate, appointmentDate);

            if (!isValidDayAndRecurrence) {
                return false;
            }
        }

        // Verificar rango horario
        return !appointmentTime.isBefore(eventStartTime) && !appointmentTime.isAfter(eventEndTime);
    }




    /**
     * Verifica si una fecha determinada {@code currentDate} cumple con el patrón
     * de recurrencia definido a partir de una fecha de inicio {@code startDate}.
     *
     * <p>
     * Este método evalúa únicamente la lógica de recurrencia temporal
     * (diaria, semanal, quincenal, mensual, anual o puntual).
     * </p>
     *
     * <p><b>Importante:</b></p>
     * <ul>
     *   <li>No valida días de la semana ({@code days}).</li>
     *   <li>No valida rangos horarios.</li>
     *   <li>No valida límites de fin del bloqueo.</li>
     * </ul>
     *
     * <p>
     * La validación del día de la semana debe realizarse de forma independiente,
     * ya que el patrón de día no está definido por la recurrencia sino por la
     * configuración explícita del bloqueo.
     * </p>
     *
     * @param startDate   fecha desde la cual comienza a aplicarse la recurrencia
     * @param currentDate fecha a evaluar contra el patrón de recurrencia
     * @param recurrence  tipo de recurrencia configurada
     * @return {@code true} si {@code currentDate} cumple con el patrón de recurrencia,
     *         {@code false} en caso contrario
     */
    public static boolean matchesRecurrence(
            LocalDate startDate,
            LocalDate currentDate,
            CalendarLockRecurrenceName recurrence
    ) {
        switch (recurrence) {

            /**
             * NONE – Bloqueo puntual sin recurrencia.
             *
             * Evalúa:
             * - Retorna TRUE únicamente si la fecha evaluada coincide exactamente
             *   con la fecha de inicio del bloqueo.
             *
             * No evalúa:
             * - Rango de fechas
             * - Días de la semana
             * - Intervalos temporales
             *
             * Uso esperado:
             * - Bloqueos de un solo día específico.
             */
            case NONE:
                return currentDate.equals(startDate);

            /**
             * DAILY – Bloqueo diario.
             *
             * Evalúa:
             * - Retorna TRUE para cualquier fecha evaluada.
             *
             * No evalúa:
             * - Si la fecha es anterior o posterior al inicio
             * - Rango de fechas
             * - Días específicos
             *
             * Importante:
             * - El control de rango (startDate / endDate) debe realizarse
             *   por fuera de este método.
             *
             * Uso esperado:
             * - Vacaciones, licencias médicas u ausencias continuas.
             */
            case DAILY:
                return true;

            /**
             * WEEKLY – Bloqueo semanal.
             *
             * Evalúa:
             * - Retorna TRUE si la fecha evaluada es IGUAL o POSTERIOR
             *   a la fecha de inicio del bloqueo.
             *
             * No evalúa:
             * - Coincidencia del día de la semana
             * - Intervalo exacto de repetición semanal
             * - Lista de días configurados
             *
             * Importante:
             * - Esta validación NO determina si la fecha corresponde
             *   al patrón semanal.
             * - La coincidencia del día (lunes, martes, etc.)
             *   debe validarse externamente.
             *
             * En la práctica:
             * - Esta lógica solo valida "no ir hacia atrás en el tiempo".
             */
            case WEEKLY:
                return ChronoUnit.WEEKS.between(startDate, currentDate) >= 0;

            /**
             * BIWEEKLY – Bloqueo quincenal.
             *
             * Evalúa:
             * - Calcula la cantidad de semanas completas transcurridas
             *   entre la fecha de inicio y la fecha evaluada.
             * - Retorna TRUE si la cantidad de semanas es múltiplo de 2.
             *
             * No evalúa:
             * - Coincidencia del día de la semana
             * - Si la fecha es anterior al inicio
             * - Lista de días configurados
             *
             * Importante:
             * - Es necesario validar el día de la semana, sino esta lógica puede
             *   aceptar fechas incorrectas dentro del ciclo quincenal.
             */
            case BIWEEKLY:
                return ChronoUnit.WEEKS.between(startDate, currentDate) % 2 == 0;

            /**
             * MONTHLY – Bloqueo mensual.
             *
             * Evalúa:
             * - Retorna TRUE si el día del mes de la fecha evaluada
             *   coincide con el día del mes de la fecha de inicio.
             *
             * No evalúa:
             * - Si la fecha es anterior al inicio
             * - Existencia real del día (ej. 31 en febrero)
             * - Semana del mes
             *
             * Uso esperado:
             * - Bloqueos que se repiten un día fijo del mes.
             */
            case MONTHLY:
                return startDate.getDayOfMonth() == currentDate.getDayOfMonth();

            /**
             * YEARLY – Bloqueo anual.
             *
             * Evalúa:
             * - Retorna TRUE si el mes y el día de la fecha evaluada
             *   coinciden exactamente con la fecha de inicio.
             *
             * No evalúa:
             * - Si la fecha es anterior al inicio
             * - Años bisiestos u otras validaciones de calendario
             *
             * Uso esperado:
             * - Fechas fijas anuales (ej. feriados personales).
             */
            case YEARLY:
                return startDate.getMonth() == currentDate.getMonth()
                        && startDate.getDayOfMonth() == currentDate.getDayOfMonth();

            default:
                return false;
        }

    }








}
