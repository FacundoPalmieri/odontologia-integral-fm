package com.odontologiaintegralfm.feature.consultation.catalogs.treatment.model;

import com.odontologiaintegralfm.shared.model.AuditableJPA;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import java.util.Set;

/**
 * Entidad que representa a los tratamientos de los Odontólogos.
 * El mismo incluye si el tratamiento es Pre-Existente o Requerido.
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "treatment")
@Where(clause = "enabled = true")
public class Treatment extends AuditableJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(length = 50, nullable = false, unique = true)
    private String label;

    @Column(length = 50, nullable = false, unique = true)
    private String name;


    /** No todos los tratamientos son requeridos o preexistentes. */
    @ManyToMany(fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @JoinTable(
            name = "treatments_conditions",
            joinColumns = @JoinColumn(name = "treatment_id"),
            inverseJoinColumns = @JoinColumn(name = "condition_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"treatment_id","condition_id"})
    )
    private Set<TreatmentCondition> condition;
}
