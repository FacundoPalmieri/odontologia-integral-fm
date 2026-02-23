package com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.service;



import com.odontologiaintegralfm.feature.appointmentscheduling.shared.DayName;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.dto.DentistCalendarLockContextInternalDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.dto.DentistCalendarLockRequestCreateDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.dto.DentistCalendarLockRequestUpdateDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.dto.DentistCalendarLockResponseDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.calendar.enums.CalendarLockRecurrenceName;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.model.DentistCalendarLock;
import com.odontologiaintegralfm.shared.dto.Response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface IDentistCalendarLockService {

    /**
     * Método para crear una relación entre dentista y evento de bloqueo de agenda.
     * @param dentistCalendarLockContextInternalDTO  : DTO interno del servicio DentistCalendarLock. Se utiliza para preparar el contexto de validación de dentist y su jornada actual, antes de Crear una nueva, o hacer un preview de los posibles conflictos ante la intención de actualizar la misma.
     * @param dentistCalendarLockRequestCreateDTO : Datos del evento.
     */
    DentistCalendarLock create (DentistCalendarLockContextInternalDTO dentistCalendarLockContextInternalDTO, DentistCalendarLockRequestCreateDTO dentistCalendarLockRequestCreateDTO);





    /**
     * Método para actualizar una relación entre dentista y evento de bloqueo de agenda.
     * @param dentistCalendarLockRequestUpdateDTO : Datos del evento.
     */
    DentistCalendarLock update (DentistCalendarLockRequestUpdateDTO dentistCalendarLockRequestUpdateDTO);





    /**
     * Método para obtener todos los bloqueos que corresponde solo a una fecha dada.
     * @param dentistId : id dentista
     * @param date : fecha a consulta por bloqueo.
     */
    List<DentistCalendarLock> getByDate(Long dentistId, LocalDate date);

    /**
     * Método para obtener todos los bloqueos que corresponde a una semana
     * @param dentistId : id dentista
     * @param weekStart : Fecha inicio semana a consulta por bloqueo.
     * @param weekEnd   : Fecha fin semana a consulta por bloqueo.
     */
    Map<LocalDate, List<DentistCalendarLock>> getByDateRange(Long dentistId, LocalDate weekStart, LocalDate weekEnd);



    /**
     * Valída si una fecha y hora se encuentran bloqueadas por un dentista.
     * Si existe, no realiza acción.
     * Si no existe, arroja exceptión.
     * @param idDentist : Id dentista
     * @param dateTime : Fecha y hora.
     */
    void validateByIdDentistAndDateTime(Long idDentist, LocalDateTime dateTime);


    /**
     * Valída que no exista ya un bloqueo de agenda con la misma configuración
     * (fecha inicio/fin, recurrencia y al menos un día en común).
     * @param idPerson : Id dentista.
     * @param dentistCalendarLockRequestCreateDTO : Objeto nuevo a crear.
     */
    void verifyLockMatchWithLock(Long idPerson, DentistCalendarLockRequestCreateDTO dentistCalendarLockRequestCreateDTO);


    /**
     * Valída la coherencia entre el tipo de bloqueo de calendario y los datos enviados en el request de creación del bloqueo.

     * <p><b>Reglas para tipos de bloqueo con ausencia total:</b></p>
     * <ul>
     *   <li>No se permiten días específicos ({@code days} debe ser {@code null}).</li>
     *   <li>La recurrencia solo puede ser {@code DAILY} o {@code NONE}.
     *       Cualquier otro valor es inválido.</li>
     *   <li>No se permiten horarios personalizados:
     *       {@code startTime} y {@code endTime} deben ser {@code null}.</li>
     * </ul>**
     * @param dentistCalendarLockRequestCreateDTO DTO con el modo del bloqueo que se desea crear.
     *
     */
    void validateByMode(DentistCalendarLockRequestCreateDTO dentistCalendarLockRequestCreateDTO);


    /**
     * Valída que el rango de fechas enviado (startDate - endDate) sea compatible con la recurrencia seleccionada para un bloqueo de agenda.
     * <p>Reglas:
     * <ul>
     *   <li><b>WEEKLY:</b> el rango debe cubrir al menos 7 días.</li>
     *   <li><b>BIWEEKLY:</b> el rango debe cubrir al menos 14 días.</li>
     *   <li><b>MONTHLY:</b> endDate debe ser al menos un mes posterior a startDate.</li>
     *   <li><b>YEARLY:</b> endDate debe ser al menos un año posterior a startDate.</li>
     * </ul>
     * @param recurrence tipo de recurrencia seleccionada (se asume no nulo)
     * @param startDate fecha de inicio del bloqueo
     * @param endDate fecha de fin del bloqueo
     */
    void validateRecurrenceRange(CalendarLockRecurrenceName recurrence, LocalDate startDate, LocalDate endDate);







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
     */
    List<LocalDate> generateEffectiveDates(
            LocalDate realStartDate,  // Ancla real
            LocalDate startDate,      // Inicio del rango a evaluar (para cada vista)
            LocalDate endDate,        // Fin del evento
            CalendarLockRecurrenceName recurrence,
            List<DayName> days
    );

}
