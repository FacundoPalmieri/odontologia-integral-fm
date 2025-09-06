package com.odontologiaintegralfm.feature.consultation.core.model;

import com.odontologiaintegralfm.feature.appointment.enums.ConsultationStatus;
import com.odontologiaintegralfm.feature.patient.core.model.Patient;
import com.odontologiaintegralfm.feature.appointment.model.Appointment;
import com.odontologiaintegralfm.shared.model.Auditable;
import com.odontologiaintegralfm.feature.dentist.core.model.Dentist;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;


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


    @Enumerated(EnumType.STRING)
    private ConsultationStatus status;

    @Column(nullable = false)
    private BigDecimal price;

    @Lob
    private String observation;

}
