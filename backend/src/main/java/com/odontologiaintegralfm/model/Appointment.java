package com.odontologiaintegralfm.model;

import com.odontologiaintegralfm.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;

/**
 * Entidad que representa un turno médico.
 */
@Entity
@Getter
@Setter
@Audited
@Table(name = "appointments")
public class Appointment extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="dentist_id")
    private Dentist dentist;

    @Column(nullable = false)
    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;

}
