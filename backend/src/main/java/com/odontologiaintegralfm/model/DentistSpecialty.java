package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidad que representa la especialidad de los Odontólogos.
 */
@Entity
@Data
@Table(name = "dental_specialties")
public class DentistSpecialty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30, unique = true,nullable = false)
    private String name;

    private Boolean enabled;
}
