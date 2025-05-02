package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidad para representar los riesgos médicos
 */

@Entity
@Data
@Table(name = "medical_risk")
public class MedicalRisk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false, unique = true)
    private String name;

    private Long parentId;

    private Boolean enabled;
}
