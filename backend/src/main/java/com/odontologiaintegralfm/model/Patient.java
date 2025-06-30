package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import java.time.LocalDateTime;


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
