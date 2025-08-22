package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

/**
 * Entidad que representa el domicilio de una Persona
 */
@Audited
@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(length = 30,nullable = false)
    private String street;

    private Integer number;

    @Column(length = 2)
    private String floor;

    @Column(length = 2)
    private String apartment;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Locality.class)
    @JoinColumn(name = "locality_id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Locality locality;

}
