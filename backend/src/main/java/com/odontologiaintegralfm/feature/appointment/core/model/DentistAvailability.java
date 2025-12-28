package com.odontologiaintegralfm.feature.appointment.core.model;

import com.odontologiaintegralfm.feature.appointment.core.enums.CalendarLockRecurrenceName;
import com.odontologiaintegralfm.feature.appointment.catalogs.enums.DayName;
import com.odontologiaintegralfm.feature.dentist.core.model.Dentist;
import com.odontologiaintegralfm.feature.user.model.UserSec;
import com.odontologiaintegralfm.shared.model.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Representa una disponibilidad laboral de un dentista.
 *
 * <p>Esta entidad admite dos modalidades excluyentes:
 *
 * <ul>
 *   <li><b>Modo recurrente</b>: (keyName + recurrence)
 *       El dentista trabaja un día de la semana (keyName) con cierta recurrencia (DAILY, WEEKLY, BIWEEKLY, MONTHLY, etc.).
 *
 *   <li><b>Modo fecha específica</b>: (specificDate)
 *       El dentista trabaja un día puntual sin repetición.
 * </ul>
 *
 * Reglas clave:
 * <ul>
 *   <li>Solo uno de los modos puede estar activo.
 *   <li>Si existe specificDate, entonces keyName y recurrence deben ser null.
 *   <li>Si existe keyName, entonces recurrence debe existir y specificDate debe ser null.
 * </ul>
 *
 * <p><b>effectiveDate</b>:
 * Fecha real en que este horario entra en vigencia.
 * <p>Cuando la disponibilidad es recurrente, effectiveDate NO viene desde el cliente:
 * se calcula en la capa de servicio buscando el primer día que coincida con keyName dentro de los próximos 7 días corridos desde la fecha de modificación.
 *
 * Ejemplo:
 * <pre>
 *   keyName = MONDAY
 *   hoy = miércoles 10/07
 *   próximos 7 días = 10/07 al 17/07
 *   el siguiente lunes es 15/07 → effectiveDate = 15/07
 * </pre>
 *
 * Cuando existe specificDate, esa misma fecha es la effectiveDate.
 */

@Entity
@Getter
@Setter
@NoArgsConstructor
@Audited
@Table(name = "dentist_availabilities")
@Where(clause = "enabled = true")
public class DentistAvailability extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dentist_id", nullable = false, updatable = false)
    private Dentist dentist;


    @Enumerated(EnumType.STRING)
    private DayName keyName;

    @Enumerated(EnumType.STRING)
    private CalendarLockRecurrenceName recurrence;

    @Column(name = "specific_date")
    private LocalDate specificDate;

    /** Fecha de inicio real. Se calcula de acuerdo al primer match de KeyName en los próximos 7 días corridos. */
    @Column(nullable = false)
    private LocalDate effectiveDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private Integer appointmentDuration; // en minutos


    /** Campos que representar un break dentro de la jornada laboral. */
    private LocalTime breakStartTime;
    private LocalTime breakEndTime;


    private DentistAvailability(Dentist dentist, DayName keyName,LocalDate specificDate,CalendarLockRecurrenceName recurrence, LocalTime startTime, LocalTime endTime, Integer appointmentDuration,LocalDate effectiveDate, LocalTime breakStartTime,LocalTime breakEndTime) {
        this.dentist = dentist;
        this.keyName = keyName;
        this.specificDate = specificDate;
        this.recurrence = recurrence;
        this.startTime = startTime;
        this.endTime = endTime;
        this.appointmentDuration = appointmentDuration;
        this.effectiveDate = effectiveDate;
        this.breakStartTime = breakStartTime;
        this.breakEndTime = breakEndTime;
    }


    public static DentistAvailability build(Dentist dentist, DayName keyName,LocalDate specificDate,CalendarLockRecurrenceName recurrence, LocalTime startTime, LocalTime endTime, Integer appointmentDuration,LocalDate effectiveDate, LocalTime breakStartTime,LocalTime breakEndTime){
        return new DentistAvailability(dentist,keyName,specificDate,recurrence,startTime,endTime,appointmentDuration,effectiveDate,breakStartTime,breakEndTime);
    }

}


