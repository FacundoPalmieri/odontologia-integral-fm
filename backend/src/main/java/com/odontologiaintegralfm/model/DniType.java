package com.odontologiaintegralfm.model;


import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidad que representa a los tipos de documento de una Persona
 * Ej: DNI - Pasaporte
 */
@Entity
@Data
@Table(name="dni_types")
public class DniType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    // Dni - Pasaporte - Etc.
    @Column(length=15, unique = true, nullable = false)
    private String name;

    private Boolean enabled;
}
