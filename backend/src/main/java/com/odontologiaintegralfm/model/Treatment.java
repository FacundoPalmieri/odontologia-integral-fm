package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidad que representa a los tratamientos de los Odontólogos.
 */
@Entity
@Data
@Table(name = "Treatment")
public class Treatment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false, unique = true)
    private String name;

    private Boolean enabled;
}
