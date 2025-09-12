package com.odontologiaintegralfm.feature.appointment.core.model;

import com.odontologiaintegralfm.feature.appointment.catalogs.enums.AppointmentStatus;
import com.odontologiaintegralfm.shared.model.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

/**
 * Entidad que representar la "bitácora" de un turno
 */
@Entity
@Getter
@Setter
@Table (name = "appointments_status_history")
@Where(clause = "enabled = true")

public class AppointmentStatusHistory extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false, updatable = false)
    private Appointment appointment;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;
}
