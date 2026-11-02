package com.odontologiaintegralfm.feature.consultation.treatmentinstance.model;

import com.odontologiaintegralfm.feature.consultation.treatment.enums.TreatmentConditionType;
import com.odontologiaintegralfm.feature.consultation.treatment.model.Treatment;
import com.odontologiaintegralfm.feature.consultation.scope.model.Scope;
import com.odontologiaintegralfm.feature.patient.model.Patient;
import com.odontologiaintegralfm.shared.model.AuditableJPA;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

/**
 * Un diagnóstico/tratamiento activo de un paciente sobre una ubicación (D-01).
 * Persiste por paciente: el dato "en qué consulta ocurrió" vive en {@link TreatmentInstanceHistory}.
 */
@Entity
@Getter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "treatments_instances", indexes = {
        @Index(name = "idx_treatment_instance_patient_id", columnList = "patient_id")
})
@Audited
@Where(clause = "enabled = true")
public class TreatmentInstance extends AuditableJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Embedded
    @AssociationOverride(name = "tooth", joinTable = @JoinTable(name = "treatments_instances_tooth",
            joinColumns = @JoinColumn(name = "treatment_instance_id")))
    private Scope scope;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_id", nullable = false)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Treatment treatment;

    @Enumerated(EnumType.STRING)
    @Column(name = "treatment_condition", nullable = false)
    private TreatmentConditionType treatmentCondition;

    private TreatmentInstance(Patient patient, Scope scope, Treatment treatment, TreatmentConditionType treatmentCondition) {
        this.patient = patient;
        this.scope = scope;
        this.treatment = treatment;
        this.treatmentCondition = treatmentCondition;
    }

    public static TreatmentInstance build(Patient patient, Scope scope, Treatment treatment, TreatmentConditionType treatmentCondition) {
        return new TreatmentInstance(patient, scope, treatment, treatmentCondition);
    }

    /** Único setter: la condición cambia por derivación o por cierre explícito (D-26, D-28, D-55). */
    public void applyCondition(TreatmentConditionType treatmentCondition) {
        this.treatmentCondition = treatmentCondition;
    }
}
