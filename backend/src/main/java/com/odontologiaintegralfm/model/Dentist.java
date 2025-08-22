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
public class Dentist extends Auditable {

    @Id
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

}
