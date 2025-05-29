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
public class Patient extends Person {

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = HealthPlan.class)
    @JoinColumn(name = "health_plan_id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private HealthPlan healthPlan;

    @Column(length = 20, unique = true, nullable = false)
    private String affiliateNumber;
}
