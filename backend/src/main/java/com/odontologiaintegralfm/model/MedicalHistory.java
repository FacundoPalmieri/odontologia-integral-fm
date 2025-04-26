package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * Entidad para representar las historias clínicas.
 */

@Entity
@Data
@Table (name = "medical_histories")
public class MedicalHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Patient.class)
    @JoinColumn(name = "patient_id",nullable = false)
    private Patient patient;

    @Column(nullable = false)
    private LocalDate dateTime;

    @Lob
    private String observation;

    private Boolean enabled;

}
