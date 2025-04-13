package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidad que representa una localidad.
 */
@Entity
@Data
@Table(name = "localities")
public class Locality {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30, unique = true,nullable = false)
    private String name;

    @ManyToOne(targetEntity = Province.class)
    @JoinColumn(name ="province_id")
    private Province province;

    @Column(nullable = false)
    private boolean enabled;
}
