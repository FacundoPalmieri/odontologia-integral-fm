package com.odontologiaintegralfm.feature.appointment.core.model;

import com.odontologiaintegralfm.feature.appointment.catalogs.enums.DayName;
import com.odontologiaintegralfm.feature.dentist.core.model.Dentist;
import com.odontologiaintegralfm.feature.user.model.UserSec;
import com.odontologiaintegralfm.shared.model.Auditable;
import jakarta.persistence.*;
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

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private Integer appointmentDuration; // en minutos


    public DentistAvailability(Dentist dentist, DayName keyName, LocalTime startTime, LocalTime endTime, Integer appointmentDuration, LocalDateTime createAt, UserSec createBy, boolean enabled) {
        this.dentist = dentist;
        this.keyName = keyName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.appointmentDuration = appointmentDuration;
        this.setCreatedAt(createAt);
        this.setCreatedBy(createBy);
        this.setEnabled(enabled);

    }

}


