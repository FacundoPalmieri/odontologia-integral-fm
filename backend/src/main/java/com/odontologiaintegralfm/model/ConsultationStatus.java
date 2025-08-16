package com.odontologiaintegralfm.model;

import com.odontologiaintegralfm.enums.ConsultationStatusName;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 * Entidad que representa los diferentes estados que puede tener la consulta.
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Audited
@Table(name = "consultation_status")
public class ConsultationStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true,updatable = false, length = 50)
    private ConsultationStatusName nameKey; //Ej: Iniciado, en Proceso

    @Column(unique = true, nullable = false, length = 50)
    private String label;
}
