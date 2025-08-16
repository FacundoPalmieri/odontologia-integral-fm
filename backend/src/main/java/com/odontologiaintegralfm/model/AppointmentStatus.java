package com.odontologiaintegralfm.model;

import com.odontologiaintegralfm.enums.AppointmentStatusName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 * Entidad que representa los posibles estados de un turno.
 */
@Entity
@Getter
@Setter
@Audited
@Table(name = "appointment_status")
public class AppointmentStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 50)
    private AppointmentStatusName statusKey;

    @Column(nullable = false, length = 50)
    private String label;

}
