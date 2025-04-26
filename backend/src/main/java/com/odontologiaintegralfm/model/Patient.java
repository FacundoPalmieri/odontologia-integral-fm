package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * Entidad que representa los pacientes.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name="patients")
public class Patient extends Person {

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = HealthPlans.class)
    @JoinColumn(name = "health_plans_id")
    private HealthPlans healthPlans;

    @Column(length = 20, unique = true, nullable = false)
    private String affiliateNumber;

    private Boolean enabled;

}
