package com.odontologiaintegralfm.feature.appointment.core.model;

import com.odontologiaintegralfm.feature.appointment.core.enums.AppointmentStatus;
import com.odontologiaintegralfm.feature.dentist.core.model.Dentist;
import com.odontologiaintegralfm.feature.patient.core.model.Patient;
import com.odontologiaintegralfm.feature.user.model.UserSec;
import com.odontologiaintegralfm.shared.model.Auditable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;
import java.time.LocalDateTime;

/**
 * Entidad que representa un turno médico.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Audited
@Table(name = "appointments")
@Where(clause = "enabled = true")
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


   private Appointment(Patient patient, Dentist dentist, LocalDateTime date, AppointmentStatus status) {
        this.patient = patient;
        this.dentist = dentist;
        this.date = date;
        this.status = status;
   }

   public static Appointment build(Patient patient, Dentist dentist, LocalDateTime date, AppointmentStatus status){
       return new Appointment(
               patient,
               dentist,
               date,
               status
       );
   }

}
