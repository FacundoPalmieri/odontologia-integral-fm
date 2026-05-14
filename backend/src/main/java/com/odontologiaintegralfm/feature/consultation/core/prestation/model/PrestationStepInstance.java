package com.odontologiaintegralfm.feature.consultation.core.prestation.model;

import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.model.ConsultationInstance;
import com.odontologiaintegralfm.feature.consultation.core.prestation.enums.PrestationStepStatus;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.model.PrestationStep;
import com.odontologiaintegralfm.shared.model.AuditableJPA;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;

/**
 * Entidad que representa una instancia de paso dentro de una prestación.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "prestations_steps_instances")
@Where(clause = "enabled = true")
@Audited
public class PrestationStepInstance extends AuditableJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    //Identificar en que consulta se creó la prestación.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestation_instance_id", nullable = false)
    private PrestationInstance prestationInstance;

    //Para identificar x consulta los avances en los pasos.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_instance_id", nullable = false)
    private ConsultationInstance consultationInstance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestation_step_id", nullable = false)
    private PrestationStep step;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrestationStepStatus status;

    private PrestationStepInstance(PrestationInstance prestationInstance, ConsultationInstance consultationInstance, PrestationStep step, PrestationStepStatus status) {
        this.prestationInstance = prestationInstance;
        this.consultationInstance = consultationInstance;
        this.step = step;
        this.status = status;
    }

    public static PrestationStepInstance build(PrestationInstance prestationInstance, ConsultationInstance consultationInstance, PrestationStep step, PrestationStepStatus status) {
        return new PrestationStepInstance(prestationInstance,consultationInstance, step, status);
    }
}

