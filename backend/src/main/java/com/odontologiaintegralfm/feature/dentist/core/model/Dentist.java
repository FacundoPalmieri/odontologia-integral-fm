package com.odontologiaintegralfm.feature.dentist.core.model;

import com.odontologiaintegralfm.feature.dentist.catalogs.model.DentistSpecialty;
import com.odontologiaintegralfm.feature.person.core.model.Person;
import com.odontologiaintegralfm.shared.model.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;
import org.hibernate.sql.ast.Clause;

import java.time.LocalTime;

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
@Where(clause = "enabled = true")
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
