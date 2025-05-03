package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import lombok.Data;


/**
 * Entidad que representa los tipos de teléfonos.
 * Ej: Celular - Fijo.
 */
@Entity
@Data
@Table
public class PhoneType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Celular - Fijo
    @Column(length = 10, unique = true, nullable = false)
    private String name;

    private Boolean enabled;
}
