package com.odontologiaintegralfm.model;


import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 * Entidad que representa a los tipos de documento de una Persona
 * Ej: DNI - Pasaporte
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name="dni_types")
public class DniType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long Id;

    // Dni - Pasaporte - Etc.
    @Column(length=15, unique = true, nullable = false)
    private String name;

    private Boolean enabled;
}
