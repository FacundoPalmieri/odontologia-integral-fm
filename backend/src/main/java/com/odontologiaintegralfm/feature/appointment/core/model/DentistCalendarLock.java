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
 * Representa un bloqueo de agenda por un odontólogo.
 *
 * La combinación de fechas, horarios, días y recurrencia define cómo se aplicará el bloqueo en el calendario.
 *
 * Existen tres modalidades principales de bloqueo:
 *
 * 1) **Bloqueo puntual (solo un día)**
 *    - days: vacío
 *    - recurrence: null
 *    - startDate == endDate
 *    -> Se interpreta como un bloqueo para un único día específico.
 *    Persistencia:
 *    - recurrence: NONE
 *    - DentistCalendarDetail: No se registra nada.
 *
 * 2) **Bloqueo de varios días en una misma semana sin recurrencia**
 *    - days: no vacío.
 *    - recurrence: null
 *    - startDate != endDate
 *    -> Se interpreta como un bloqueo para varios dias en una misma semana sin recurrencia
 *    Persistencia:
 *    - recurrence: NONE
 *    - DentistCalendarDetail: Se registran los dias
 *
 * 3) **Bloqueo diario automático (vacaciones u ausencias prolongadas)**
 *    - days: vacío
 *    - recurrence: null o DAILY
 *    - startDate != endDate
 *    -> Se genera un bloqueo todos los días entre startDate y endDate
 *    Persistencia:
 *    - recurrence: DAILY
 *    - DentistCalendarDetail: No se registra nada.
 *
 * 4) **Bloqueo recurrente semanal o con patrón**
 *    - days: no vacío
 *    - recurrence: WEEKLY, MONTHLY, etc.
 *    -> Se generan bloqueos según los días indicados dentro del rango.
 *    Persistencia:
 *    - recurrence: La correspondiente
 *    - DentistCalendarDetail: Un registro por cada día.
 *
 * Reglas de validación:
 * - Si days está vacío:
 *      - recurrence solo puede ser NONE o DAILY.
 *      - Si startDate == endDate → bloqueo puntual (recurrence = NONE).
 *      - Si startDate != endDate y recurrence es null → recurrence = DAILY.
 *
 * - Si days NO está vacío:
 *      - recurrence DAILY es inválido.
 *      - Si recurrence es null -> se toma recurrence = NONE.
 *
 * startDate y endDate funcionan como “semanas ancla” desde donde se toman los días cuando el bloqueo supera más de un día.
 *
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

}
