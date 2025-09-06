package com.odontologiaintegralfm.feature.person.catalogs.model;

import com.odontologiaintegralfm.shared.model.Auditable;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad que representa un país
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name= "countries")
public class Country extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(length = 15, unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean enabled;
}
