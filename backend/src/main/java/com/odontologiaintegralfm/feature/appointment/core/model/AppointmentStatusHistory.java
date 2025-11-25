package com.odontologiaintegralfm.feature.appointment.core.model;

import com.odontologiaintegralfm.feature.appointment.core.enums.AppointmentActionRequester;
import com.odontologiaintegralfm.feature.appointment.core.enums.AppointmentStatus;
import com.odontologiaintegralfm.feature.user.model.UserSec;
import com.odontologiaintegralfm.shared.model.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

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


    public AppointmentStatusHistory(Appointment appointment, AppointmentStatus status,AppointmentActionRequester requestedBy,String observation, UserSec createBy, LocalDateTime createAt, boolean enabled) {
        this.appointment = appointment;
        this.status = status;
        this.requestedBy = requestedBy;
        this.observation = observation;
        this.setCreatedBy(createBy);
        this.setCreatedAt(createAt);
        this.setEnabled(enabled);
    }

}
