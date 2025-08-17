package com.odontologiaintegralfm.model;

import com.odontologiaintegralfm.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad que representar la "bitácora" de un turno
 */
@Entity
@Getter
@Setter
@Table (name = "appointments_status_history")
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
