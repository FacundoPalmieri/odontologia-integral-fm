package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

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

    @OneToOne(fetch = FetchType.LAZY, targetEntity = Patient.class)
    @JoinColumn(name = "patient_id",nullable = false)
    private Patient patient;

    @Column(nullable = false)
    private LocalDate dateTime;

    @ManyToMany(fetch = FetchType.LAZY, targetEntity = MedicalRisk.class)
    @JoinTable(
            name = "medical_histories_medical_risk",
            joinColumns = @JoinColumn(name = "medical_histories_id"),
            inverseJoinColumns = @JoinColumn(name = "medical_risk_id")
    )
    private Set<MedicalRisk> medicalRisks = new HashSet<>();

    private Boolean enabled;

}
