package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidad que representa el domicilio de una Persona
 */
@Entity
@Data
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30,nullable = false)
    private String street;

    private Integer number;

    @Column(length = 2)
    private String floor;

    @Column(length = 2)
    private String apartment;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Locality.class)
    @JoinColumn(name = "locality_id")
    private Locality locality;

    private Boolean enabled;

}
