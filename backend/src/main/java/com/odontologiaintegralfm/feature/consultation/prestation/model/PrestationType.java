package com.odontologiaintegralfm.feature.consultation.prestation.model;

import com.odontologiaintegralfm.feature.consultation.prestation.enums.PrestationScopeType;
import com.odontologiaintegralfm.feature.consultation.treatment.model.Treatment;
import com.odontologiaintegralfm.feature.dentist.model.DentistSpecialty;
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

    /** Texto visible y editable; `name` queda como key estable (D-49). */
    @Column(nullable = false)
    private String label;

    @ManyToMany(fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @JoinTable(
            name = "prestacion_treatment",
            joinColumns = @JoinColumn(name = "prestacion_id"),
            inverseJoinColumns = @JoinColumn(name = "treatment_id")
    )
    private Set<Treatment> treatments;

    @ManyToMany(fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @JoinTable(
            name = "prestation_types_dentist_specialties",
            joinColumns = @JoinColumn(name = "prestacion_id"),
            inverseJoinColumns = @JoinColumn(name = "dentist_specialty_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"prestacion_id","dentist_specialty_id"})
    )
    private Set<DentistSpecialty> dentistSpecialties;


    // Si es true, no puede haber otra prestación en el odontogramaDetail.
    @Column(nullable = false)
    private boolean isUnique;

    @Column(name = "requires_origin_treatment", nullable = false)
    private boolean requiresOriginTreatment;

    @Column(name = "max_tooth")
    private Integer maxTooth;

    @ElementCollection(targetClass = PrestationScopeType.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
            name = "prestation_type_scopes",
            joinColumns = @JoinColumn(name = "prestation_type_id")
    )
    @Column(name = "scope_type", nullable = false)
    private Set<PrestationScopeType> allowedScopes;
}
