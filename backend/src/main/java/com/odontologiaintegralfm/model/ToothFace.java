package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidad que representa la cara de los Dientes
 */

@Entity
@Data
@Table(name = "teeth_faces")
public class ToothFace {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private int number;

    @Column(length =20, unique = true)
    private String name;

    private Boolean enabled;
}
