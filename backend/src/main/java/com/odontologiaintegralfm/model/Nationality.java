package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidad que representa las nacionalidades de las Personas.
 */
@Entity
@Data
@Table(name = "nationalities")
public class Nationality {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false, unique = true)
    private String name;

    private Boolean enabled;
}
