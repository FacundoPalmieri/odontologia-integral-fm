package com.odontologiaintegralfm.model;


import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidad que representa una provincia.
 */
@Entity
@Data
@Table(name= "provinces")
public class Province {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 15,unique=true,nullable = false)
    private String name;

    @ManyToOne(targetEntity = Country.class)
    @JoinColumn(name = "country_id")
    private Country country;

    @Column(nullable = false)
    private boolean enabled;
}
