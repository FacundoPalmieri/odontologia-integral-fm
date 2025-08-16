package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


/**
 * Entidad que representa la consulta del paciente.
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "consultations")
@Audited
public class Consultation extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Patient.class)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Dentist.class)
    @JoinColumn(name = "dentist_id")
    private Dentist dentist;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id")
    private ConsultationStatus status;

    @Column(nullable = false)
    private BigDecimal price;

    @Lob
    private String observation;

}
