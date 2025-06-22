package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


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

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @Lob
    @Column(nullable = false)
    private String observations;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = PaymentMethod.class)
    @JoinColumn(name = "payment_method_id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    private BigDecimal amount;
}
