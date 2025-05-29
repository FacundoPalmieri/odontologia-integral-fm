package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;

/**
 * Entidad que representa los Odontólogos
 */

@Audited
@Entity
@Getter
@Setter
@Table(name ="dentists", uniqueConstraints = {
        @UniqueConstraint(columnNames = "licenseNumber")
})
public class Dentist {
    @Id
    @Column(name = "person_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "person_id")
    private Person person;

    @Column(length = 30, unique = true, nullable = false)
    private String licenseNumber;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = DentistSpecialty.class)
    @JoinColumn(name = "dentist_specialty_id")
    private DentistSpecialty dentistSpecialty;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;


    @ManyToOne(fetch = FetchType.LAZY, targetEntity = UserSec.class)
    @JoinColumn(name = "created_by_id",nullable = false, updatable = false)
    private UserSec createdBy;

    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = UserSec.class)
    @JoinColumn(name = "updated_by_id")
    private UserSec updatedBy;

    @Column(nullable = false)
    private Boolean enabled;

    private LocalDateTime disabledAt;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = UserSec.class)
    @JoinColumn(name = "disabled_by_id")
    private UserSec disabledBy;


}
