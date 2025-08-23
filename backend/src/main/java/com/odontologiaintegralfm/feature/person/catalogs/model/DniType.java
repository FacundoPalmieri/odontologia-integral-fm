package com.odontologiaintegralfm.feature.person.catalogs.model;


import com.odontologiaintegralfm.shared.model.Auditable;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad que representa a los tipos de documento de una Persona
 * Ej: DNI - Pasaporte
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name="dni_types")
public class DniType extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long Id;

    // Dni - Pasaporte - Etc.
    @Column(length=15, unique = true, nullable = false)
    private String name;

}
