package com.odontologiaintegralfm.feature.appointment.core.model;

import com.odontologiaintegralfm.feature.appointment.core.enums.CalendarLockRecurrenceName;
import com.odontologiaintegralfm.feature.appointment.catalogs.model.CalendarLockType;
import com.odontologiaintegralfm.feature.dentist.core.model.Dentist;
import com.odontologiaintegralfm.shared.model.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Representa un bloqueo de agenda aplicado por un odontólogo.
 *
 * <p>
 * Un bloqueo define intervalos de tiempo en los cuales no pueden asignarse turnos,
 * y su comportamiento resulta de la combinación de fechas, horarios, días específicos
 * y recurrencia.
 * </p>
 *
 * <p><b>Escenarios funcionales soportados:</b></p>
 *
 * <ol>
 *   <li>
 *     <b>Bloqueo puntual (un único día)</b><br>
 *     - days: vacío<br>
 *     - recurrence: NONE<br>
 *     - startDate == endDate<br>
 *     Se interpreta como un bloqueo aplicado a una única fecha.
 *   </li>
 *
 *   <li>
 *     <b>Bloqueo de varios días sin recurrencia</b><br>
 *     - days: no vacío<br>
 *     - recurrence: NONE<br>
 *     - startDate != endDate<br>
 *     Se aplica únicamente a los días indicados dentro del rango definido.
 *   </li>
 *
 *   <li>
 *     <b>Bloqueo diario continuo (vacaciones o ausencias prolongadas)</b><br>
 *     - days: vacío<br>
 *     - recurrence: DAILY<br>
 *     - startDate != endDate<br>
 *     Se aplica a todos los días comprendidos entre startDate y endDate.
 *   </li>
 *
 *   <li>
 *     <b>Bloqueo recurrente con patrón</b><br>
 *     - days: no vacío<br>
 *     - recurrence: WEEKLY, MONTHLY, etc.<br>
 *     Se generan bloqueos siguiendo el patrón indicado dentro del rango de fechas.
 *   </li>
 * </ol>
 *
 * <p><b>Notas de diseño:</b></p>
 * <ul>
 *   <li>
 *     La validación de coherencia entre fechas, días y recurrencia se realiza
 *     en la capa de servicio antes de la persistencia.
 *   </li>
 *   <li>
 *     Los campos startDate y endDate representan fechas efectivas en bloqueos
 *     puntuales y diarios, y actúan como rango ancla para la evaluación de patrones
 *     en bloqueos recurrentes.
 *   </li>
 * </ul>
 */


@Entity
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "dentist_calendar_lock")
public class DentistCalendarLock extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dentist_id", nullable = false, updatable = false)
    private Dentist dentist;

    /**
     * Fecha inicial del rango del bloqueo.
     *
     * Su interpretación depende del escenario:
     *
     * - Bloqueo puntual: es la única fecha bloqueada.
     * - Bloqueo diario: inicio del período bloqueado completo.
     * - Bloqueo recurrente (WEEKLY, etc.): semana ancla desde donde se
     *   evaluarán los dayName del detalle.
     */
    @Column(nullable = false)
    private LocalDate startDate;




    /**
     * Fecha final del rango del bloqueo.
     *
     * Debe ser >= startDate.
     *
     * - Bloqueo puntual: igual a startDate.
     * - Bloqueo diario: fin del período bloqueado.
     * - Bloqueo recurrente: semana ancla final para evaluar el patrón de días.
     */
    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    private boolean isFullDay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private CalendarLockType type;


    /**
     * Frecuencia del bloqueo.
     *
     * - NONE: bloqueo puntual o patrón sin recurrencia explícita.
     * - DAILY: bloqueo todos los días (vacaciones).
     * - WEEKLY, MONTHLY, etc.: bloqueo recurrente basado en los días del detalle.
     *
     * Su valor final puede ser ajustado automáticamente según las reglas de negocio
     * en función de los valores recibidos en `days`, `startDate` y `endDate`.
     */
    @Enumerated(EnumType.STRING)
    private CalendarLockRecurrenceName recurrence;

    @Lob
    private String observation;

    @Lob
    private String observationUpdate;


    private DentistCalendarLock(Dentist dentist, LocalDate startDate, LocalDate endDate,LocalTime startTime,LocalTime endTime,boolean isFullDay,CalendarLockType type, CalendarLockRecurrenceName recurrence, String observation) {
        this.dentist = dentist;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isFullDay = isFullDay;
        this.type = type;
        this.recurrence = recurrence;
        this.observation = observation;
    }

    public static DentistCalendarLock build(Dentist dentist, LocalDate startDate, LocalDate endDate, LocalTime startTime,LocalTime endTime,boolean isFullDay ,CalendarLockType type, CalendarLockRecurrenceName recurrence, String observation){
        return new DentistCalendarLock(
                dentist,
                startDate,
                endDate,
                startTime,
                endTime,
                isFullDay,
                type,
                recurrence,
                observation
        );
    }




}
