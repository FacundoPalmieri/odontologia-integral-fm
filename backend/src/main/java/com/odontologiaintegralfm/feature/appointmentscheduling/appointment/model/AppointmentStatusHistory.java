package com.odontologiaintegralfm.feature.appointmentscheduling.appointment.model;

import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.enums.AppointmentActionRequester;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.enums.AppointmentStatus;
import com.odontologiaintegralfm.shared.model.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

/**
 * Entidad que representar la "bitácora" de un turno
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
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


    @Enumerated(EnumType.STRING)
    private AppointmentActionRequester requestedBy;

    @Lob
    private String observation;


    private AppointmentStatusHistory(Appointment appointment, AppointmentStatus status,AppointmentActionRequester requestedBy,String observation) {
        this.appointment = appointment;
        this.status = status;
        this.requestedBy = requestedBy;
        this.observation = observation;
    }

    public static AppointmentStatusHistory build(Appointment appointment, AppointmentStatus status,AppointmentActionRequester requestedBy,String observation){
        return new AppointmentStatusHistory(appointment,status,requestedBy,observation);
    }



}
