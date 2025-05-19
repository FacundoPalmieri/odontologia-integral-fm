package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;


/**
 * Entidad que representa los tipos de teléfonos.
 * Ej: Celular - Fijo.
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table
public class PhoneType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // Celular - Fijo
    @Column(length = 10, unique = true, nullable = false)
    private String name;

    private Boolean enabled;
}
