package com.odontologiaintegralfm.feature.consultation.core.consultation.model;

import com.odontologiaintegralfm.feature.consultation.core.consultation.enums.ConsultationStatusType;
import com.odontologiaintegralfm.feature.patient.core.model.Patient;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.model.Appointment;
import com.odontologiaintegralfm.feature.dentist.core.model.Dentist;
import com.odontologiaintegralfm.shared.model.AuditableJPA;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;


/**
 * Entidad que representa la consulta del paciente.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Audited
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Where(clause = "enabled = true")

@Table(name = "consultations", indexes = {
        @Index(name = "idx_consultation_patient_id", columnList = "patient_id"),
        @Index(name = "idx_consultation_dentist_id", columnList = "dentist_id"),
        @Index(name = "idx_consultation_status", columnList = "status")
})
public class Consultation extends AuditableJPA {

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
    @JoinColumn(name = "appointment_id", unique = true)
    private Appointment appointment;


    @Enumerated(EnumType.STRING)
    private ConsultationStatusType status;


    private Consultation(Patient patient, Dentist dentist, Appointment appointment, ConsultationStatusType status) {
        this.patient = patient;
        this.dentist = dentist;
        this.appointment = appointment;
        this.status = status;
    };

    public static Consultation build(Appointment appointment) {
        return new Consultation(
                appointment.getPatient(),
                appointment.getDentist(),
                appointment,
                ConsultationStatusType.WAITING_ROOM
        );
    }
}
