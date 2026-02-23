package com.odontologiaintegralfm.feature.appointmentscheduling.appointment.model;

import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.enums.AppointmentStatus;
import com.odontologiaintegralfm.feature.dentist.core.model.Dentist;
import com.odontologiaintegralfm.feature.patient.core.model.Patient;
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



    /**
     * Turno del cual este turno fue reprogramado.
     * Se usa ManyToOne porque un turno original puede dar lugar a varios turnos reprogramados.
     * La regla de “un solo turno activo por original” se valida en la capa de servicio, no en la base.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rescheduled_from_id")
    private Appointment rescheduledFrom;



    private Appointment(Patient patient, Dentist dentist, LocalDateTime date, AppointmentStatus status) {
        this.patient = patient;
        this.dentist = dentist;
        this.date = date;
        this.status = status;
   }

   public static Appointment build(Patient patient, Dentist dentist, LocalDateTime date, AppointmentStatus status) {
       return new Appointment(
               patient,
               dentist,
               date,
               status
       );
   }

}
