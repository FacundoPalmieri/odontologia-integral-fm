package com.odontologiaintegralfm.feature.consultation.catalogs.model;

import com.odontologiaintegralfm.shared.model.Auditable;
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
 * Entidad que representa la relación entre una prestación y sus pasos.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(
        name = "prestation_steps",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"prestation_id", "order"})
        }
)
@Where(clause = "enabled = true")
@Audited
public class PrestationStep extends AuditableJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestation_id", nullable = false)
    private PrestationType prestation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id", nullable = false)
    private Step step;

    @Column(name = "position", nullable = false)
    private int position;

    @Column(nullable = false)
    private boolean required;

}

