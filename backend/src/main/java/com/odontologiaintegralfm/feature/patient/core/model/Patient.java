package com.odontologiaintegralfm.feature.patient.core.model;

import com.odontologiaintegralfm.feature.patient.catalogs.model.HealthPlan;
import com.odontologiaintegralfm.feature.person.core.model.Person;
import com.odontologiaintegralfm.shared.model.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;



/**
 * Entidad que representa los pacientes.
 */
@Audited
@Entity
@Getter
@Setter
@Table(name="patients", uniqueConstraints = {
        @UniqueConstraint(columnNames = "affiliateNumber")
})
public class Patient extends Auditable {

    @Id
    private Long id; // Es el mismo que el de Person.

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id")
    @MapsId  // Mapea el id de persona a paciente.
    private Person person;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = HealthPlan.class)
    @JoinColumn(name = "health_plan_id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private HealthPlan healthPlan;

    @Column(length = 20, unique = true)
    private String affiliateNumber;
}
