package com.odontologiaintegralfm.feature.consultation.treatmentinstance.model;

import com.odontologiaintegralfm.feature.consultation.treatment.enums.TreatmentConditionType;
import com.odontologiaintegralfm.feature.consultation.consultationinstance.model.ConsultationInstance;
import com.odontologiaintegralfm.shared.model.AuditableJPA;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Historial de la condición de un {@link TreatmentInstance}: una fila por alta o transición,
 * con la consulta donde ocurrió (D-12, D-16, ADR-0032).
 */
@Entity
@Getter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "treatments_instances_history", indexes = {
        @Index(name = "idx_treatment_instance_history_treatment_instance_id", columnList = "treatment_instance_id"),
        @Index(name = "idx_treatment_instance_history_consultation_instance_id", columnList = "consultation_instance_id")
})
public class TreatmentInstanceHistory extends AuditableJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_instance_id", nullable = false)
    private TreatmentInstance treatmentInstance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_instance_id", nullable = false)
    private ConsultationInstance consultationInstance;

    @Enumerated(EnumType.STRING)
    @Column(name = "treatment_condition", nullable = false)
    private TreatmentConditionType treatmentCondition;

    private TreatmentInstanceHistory(TreatmentInstance treatmentInstance, ConsultationInstance consultationInstance, TreatmentConditionType treatmentCondition) {
        this.treatmentInstance = treatmentInstance;
        this.consultationInstance = consultationInstance;
        this.treatmentCondition = treatmentCondition;
    }

    public static TreatmentInstanceHistory build(TreatmentInstance treatmentInstance, ConsultationInstance consultationInstance, TreatmentConditionType treatmentCondition) {
        return new TreatmentInstanceHistory(treatmentInstance, consultationInstance, treatmentCondition);
    }
}
