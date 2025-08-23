package com.odontologiaintegralfm.feature.patient.core.model;

import com.odontologiaintegralfm.feature.patient.catalogs.model.MedicalRisk;
import com.odontologiaintegralfm.shared.model.Auditable;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;


/**
 * Entidad para representar los riesgos médicos de un paciente.
 */
@Entity
@Audited
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "patient_medical_risks")
public class PatientMedicalRisk extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Patient.class)
    @JoinColumn(name = "patient_id",nullable = false, updatable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = MedicalRisk.class)
    @JoinColumn(name = "medical_risk_id",nullable = false, updatable = false)
    private MedicalRisk medicalRisk;

    @Size(max = 500)
    private String observation;

}
