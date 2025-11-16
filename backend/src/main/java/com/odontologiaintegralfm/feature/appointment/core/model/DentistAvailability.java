package com.odontologiaintegralfm.feature.appointment.core.model;

import com.odontologiaintegralfm.feature.appointment.catalogs.enums.CalendarLockRecurrenceName;
import com.odontologiaintegralfm.feature.appointment.catalogs.enums.DayName;
import com.odontologiaintegralfm.feature.dentist.core.model.Dentist;
import com.odontologiaintegralfm.feature.user.model.UserSec;
import com.odontologiaintegralfm.shared.model.Auditable;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entidad que representa los días y horarios semanales disponibles de un dentista.
 * Funciona como template.
 *
 * Si recurrence = WEEKLY -> Se usa keyName para generar slots todas las semanas.
 * Si recurrence = NONE y specificDate != null -> generar disponibilidad solo ese día.
 * Si recurrence = MONTHLY -> cada mes, el mismo día del mes que specificDate.
 * Si recurrence = YEARLY -> cada año en esa misma fecha (ej: evento fijo anual).
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

    @Column(name = "specific_date")
    private LocalDate specificDate;

    @Enumerated(EnumType.STRING)
    private CalendarLockRecurrenceName recurrence;

    /** Fecha ancla para inicio de jornada laboral (día posterior a la actualización) */
    private LocalDate effectiveDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private Integer appointmentDuration; // en minutos


    public DentistAvailability(Dentist dentist, DayName keyName,LocalDate specificDate,CalendarLockRecurrenceName recurrence, LocalTime startTime, LocalTime endTime, Integer appointmentDuration,LocalDate effectiveDate ,LocalDateTime createAt, UserSec createBy, boolean enabled) {
        this.dentist = dentist;
        this.keyName = keyName;
        this.specificDate = specificDate;
        this.recurrence = recurrence;
        this.startTime = startTime;
        this.endTime = endTime;
        this.appointmentDuration = appointmentDuration;
        this.effectiveDate = effectiveDate;
        this.setCreatedAt(createAt);
        this.setCreatedBy(createBy);
        this.setEnabled(enabled);
    }

}


