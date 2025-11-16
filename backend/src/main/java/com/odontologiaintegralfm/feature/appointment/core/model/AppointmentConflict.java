package com.odontologiaintegralfm.feature.appointment.core.model;

import com.odontologiaintegralfm.feature.appointment.core.enums.OriginConflict;
import com.odontologiaintegralfm.feature.user.model.UserSec;
import com.odontologiaintegralfm.shared.model.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad que representa posibles conflictos en turnos por cambios en la configuración del dentista.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "appointment_conflicts")
public class AppointmentConflict extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Appointment appointment;

    /**Id de la tabla DentistAvailability o DentistCalendarLock */
    @Column(nullable = false)
    private Long idOriginConflict;

    @Enumerated(EnumType.STRING)
    private OriginConflict originConflict;

    @Column(nullable = false)
    private boolean resolved;


    public AppointmentConflict(Long id, Appointment appointment,Long idOriginConflict,String originConflict, LocalDateTime createAt, UserSec createBy, boolean enabled) {
        this.id = id;
        this.appointment = appointment;
        this.idOriginConflict = idOriginConflict;
        this.originConflict = OriginConflict.valueOf(originConflict);
        this.resolved = false;
        this.setCreatedAt(createAt);
        this.setCreatedBy(createBy);
        this.setEnabled(enabled);
    }
}
