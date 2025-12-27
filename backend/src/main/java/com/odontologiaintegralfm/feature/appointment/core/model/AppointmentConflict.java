package com.odontologiaintegralfm.feature.appointment.core.model;

import com.odontologiaintegralfm.feature.appointment.core.enums.OriginConflict;
import com.odontologiaintegralfm.feature.user.model.UserSec;
import com.odontologiaintegralfm.shared.model.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;

/**
 * Entidad que representa posibles conflictos en turnos por cambios en la configuración del dentista.
 */
@Entity
@Audited
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


    private AppointmentConflict(Appointment appointment,Long idOriginConflict,String originConflict) {
        this.appointment = appointment;
        this.idOriginConflict = idOriginConflict;
        this.originConflict = OriginConflict.valueOf(originConflict);
        this.resolved = false;
    }

    public static AppointmentConflict build(Appointment appointment,Long idOriginConflict,String originConflict){
        return new AppointmentConflict(appointment,idOriginConflict,originConflict);
    }
}
