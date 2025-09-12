package com.odontologiaintegralfm.feature.appointment.core.model;

import com.odontologiaintegralfm.feature.appointment.core.enums.AppointmentConflictReason;
import com.odontologiaintegralfm.shared.model.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad que representa posibles conflictos en turnos por cambios en la configuración del dentista.
 */
@Entity
@Getter
@Setter
@Table(name = "appointment_conflicts")
public class AppointmentConflict extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Appointment appointment;

    @Enumerated(EnumType.STRING)
    private AppointmentConflictReason appointmentConflictReason;

    @Column(nullable = false)
    private boolean resolved;


}
