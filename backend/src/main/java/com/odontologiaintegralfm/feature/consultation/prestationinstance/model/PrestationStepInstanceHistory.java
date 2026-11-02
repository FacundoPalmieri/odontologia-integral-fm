package com.odontologiaintegralfm.feature.consultation.prestationinstance.model;

import com.odontologiaintegralfm.feature.consultation.consultationinstance.model.ConsultationInstance;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.enums.PrestationStepStatus;
import com.odontologiaintegralfm.shared.model.AuditableJPA;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Historial del estado de un {@link PrestationStepInstance}: una fila por alta o avance,
 * con la consulta donde ocurrió (D-13, D-16, ADR-0032).
 */
@Entity
@Getter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "prestations_steps_instances_history", indexes = {
        @Index(name = "idx_prestation_step_instance_history_step_instance_id", columnList = "prestation_step_instance_id"),
        @Index(name = "idx_prestation_step_instance_history_consultation_instance_id", columnList = "consultation_instance_id")
})
public class PrestationStepInstanceHistory extends AuditableJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestation_step_instance_id", nullable = false)
    private PrestationStepInstance prestationStepInstance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_instance_id", nullable = false)
    private ConsultationInstance consultationInstance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrestationStepStatus status;

    private PrestationStepInstanceHistory(PrestationStepInstance prestationStepInstance, ConsultationInstance consultationInstance, PrestationStepStatus status) {
        this.prestationStepInstance = prestationStepInstance;
        this.consultationInstance = consultationInstance;
        this.status = status;
    }

    public static PrestationStepInstanceHistory build(PrestationStepInstance prestationStepInstance, ConsultationInstance consultationInstance, PrestationStepStatus status) {
        return new PrestationStepInstanceHistory(prestationStepInstance, consultationInstance, status);
    }
}
