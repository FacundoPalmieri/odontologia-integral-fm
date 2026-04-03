package com.odontologiaintegralfm.feature.consultation.core.model;

import com.odontologiaintegralfm.feature.consultation.catalogs.model.PrestationStep;
import com.odontologiaintegralfm.feature.consultation.catalogs.enums.PrestationStatus;
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
public class PrestationStepInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestation_instance_id", nullable = false)
    private PrestationInstance prestation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestation_step_id", nullable = false)
    private PrestationStep step;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrestationStatus status;

    private PrestationStepInstance(PrestationInstance prestation, PrestationStep step, PrestationStatus status) {
        this.prestation = prestation;
        this.step = step;
        this.status = status;
    }

    public static PrestationStepInstance build(PrestationInstance prestation, PrestationStep step, PrestationStatus status) {
        return new PrestationStepInstance(prestation, step, status);
    }
}

