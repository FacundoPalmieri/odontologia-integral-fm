package com.odontologiaintegralfm.feature.person.catalogs.model;

import com.odontologiaintegralfm.shared.model.Auditable;
import jakarta.persistence.*;
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
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table
public class PhoneType extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // Celular - Fijo
    @Column(length = 10, unique = true, nullable = false)
    private String name;

}
