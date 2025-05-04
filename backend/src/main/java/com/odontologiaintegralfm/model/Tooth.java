package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidad que representa a los Dientes
 */

@Entity
@Data
@Table (name ="teeth")
public class Tooth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private int number;

    @Column(length =20, unique = true)
    private String name;

    private Boolean enabled;

}
