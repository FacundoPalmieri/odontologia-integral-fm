package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;


/**
 * Entidad que representa los pacientes.
 */
@Audited
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name="patients")
public class Patient extends Person {

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = HealthPlan.class)
    @JoinColumn(name = "health_plans_id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private HealthPlan healthPlan;

    @Column(length = 20, unique = true, nullable = false)
    private String affiliateNumber;
}
