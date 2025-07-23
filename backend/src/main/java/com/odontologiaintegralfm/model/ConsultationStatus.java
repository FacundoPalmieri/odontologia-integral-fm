package com.odontologiaintegralfm.model;

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
@Table(name = "consultation_statuses")
public class ConsultationStatus extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;        //Ej: Iniciado, en Proceso

    @Column(nullable = false, unique = true, length = 255)
    private String description; // Ej Consulta iniciada por recepción

    @Column(name ="`order`" )
    private Integer order;      // Para orden visual en combos

}
