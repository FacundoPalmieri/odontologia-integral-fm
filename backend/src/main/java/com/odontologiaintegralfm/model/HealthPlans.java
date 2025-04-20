package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidad que representa las Obras Sociales
 */
@Entity
@Data
@Table(name = "health_plans")
public class HealthPlans {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(length = 50, unique = true, nullable = false)
    private String name;

    private Boolean enabled;

}
