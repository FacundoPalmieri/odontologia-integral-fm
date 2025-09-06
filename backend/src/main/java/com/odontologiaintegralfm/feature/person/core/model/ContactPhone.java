package com.odontologiaintegralfm.feature.person.core.model;

import com.odontologiaintegralfm.feature.person.catalogs.model.PhoneType;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

/**
 * Entidad que representa los contactos telefónicos de las personas
 */

@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table (name = "contacts_phones")
@Audited
public class ContactPhone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(targetEntity = PhoneType.class)
    @JoinColumn(name = "telephone_type_id")
    @Audited(targetAuditMode = NOT_AUDITED)
    private PhoneType phoneType;

    @Column(length = 20, nullable = false, unique = true)
    private String number;


}
