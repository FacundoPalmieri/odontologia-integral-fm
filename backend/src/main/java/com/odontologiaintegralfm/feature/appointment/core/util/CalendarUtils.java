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


}
