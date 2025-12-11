package com.odontologiaintegralfm.feature.appointment.core.model;

import com.odontologiaintegralfm.feature.appointment.catalogs.enums.DayName;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;


/**
 * Representa un día específico dentro de un patrón recurrente de bloqueo.
 *
 * Solo se utiliza cuando el usuario envía un conjunto de días (MONDAY, TUESDAY, etc.).
 *
 * Ejemplos:
 * - days = [MONDAY, WEDNESDAY], recurrence = WEEKLY
 *     → Se bloquean todos los lunes y miércoles dentro del rango.
 *
 * - Si el bloqueo es puntual o diario, esta entidad no se utiliza.
 */
@Entity
@Audited
@Getter
@Setter
@Table(name = "dentist_calendar_lock_details")
public class DentistCalendarLockDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dentist_calendar_lock_id", nullable = false)
    private DentistCalendarLock dentistCalendarLock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayName dayName;
}
