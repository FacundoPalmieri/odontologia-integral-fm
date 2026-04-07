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
import org.hibernate.envers.RelationTargetAuditMode;

import java.util.Set;

/**
 * Entidad que representa un catálogo de prestaciones (servicios odontológicos).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "prestations_types")
@Where(clause = "enabled = true")
@Audited
public class PrestationType extends AuditableJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String name;

    @ManyToMany(fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @JoinTable(
            name = "prestacion_treatment",
            joinColumns = @JoinColumn(name = "prestacion_id"),
            inverseJoinColumns = @JoinColumn(name = "treatment_id")
    )
    private Set<Treatment> treatments;


    // Si es true, no puede haber otra prestación en el odontogramaDetail.
    @Column(nullable = false)
    private boolean isUnique;

    @Column(nullable = false)
    private boolean hasSteps;

    @Column(nullable = false)
    private boolean requiresLocation;

    private PrestationType(String name, Set<Treatment> treatments, boolean isUnique, boolean hasSteps, boolean requiresLocation) {
        this.name = name;
        this.treatments = treatments;
        this.isUnique = isUnique;
        this.hasSteps = hasSteps;
        this.requiresLocation = requiresLocation;
    }

    public static PrestationType build(String name, Set<Treatment> treatments, boolean isUnique, boolean hasSteps, boolean requiresLocation) {
        return new PrestationType(name, treatments, isUnique, hasSteps, requiresLocation);
    }
}
