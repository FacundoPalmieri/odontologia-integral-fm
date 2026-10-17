package com.odontologiaintegralfm.feature.consultation.prestationinstance.model;

import com.odontologiaintegralfm.feature.consultation.prestationinstance.enums.PrestationStepStatus;
import com.odontologiaintegralfm.feature.consultation.prestation.model.PrestationStep;
import com.odontologiaintegralfm.shared.model.AuditableJPA;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;

/**
 * Entidad que representa una instancia de paso dentro de una prestación.
 * La consulta donde ocurrió cada transición vive en {@link PrestationStepInstanceHistory} (D-13).
 */
@Entity
@Getter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "prestations_steps_instances")
@Where(clause = "enabled = true")
@Audited
public class PrestationStepInstance extends AuditableJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestation_instance_id", nullable = false)
    private PrestationInstance prestationInstance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestation_step_id", nullable = false)
    private PrestationStep step;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrestationStepStatus status;

    private PrestationStepInstance(PrestationInstance prestationInstance, PrestationStep step, PrestationStepStatus status) {
        this.prestationInstance = prestationInstance;
        this.step = step;
        this.status = status;
    }

    public static PrestationStepInstance build(PrestationInstance prestationInstance, PrestationStep step, PrestationStepStatus status) {
        return new PrestationStepInstance(prestationInstance, step, status);
    }

    /** Único setter: el avance de un step existente (D-19: cada avance además escribe una fila de historial). */
    public void applyStatus(PrestationStepStatus status) {
        this.status = status;
    }
}
