package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa los contactos telefónicos de las personas
 */

@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table (name = "contacts_phones")
public class ContactPhone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(targetEntity = PhoneType.class)
    @JoinColumn(name = "telephone_type_id")
    private PhoneType phoneType;

    @Column(length = 20, nullable = false, unique = true)
    private String number;

    @ManyToMany(fetch = FetchType.LAZY, targetEntity = Person.class)
    @JoinTable(
            name = "contact_phone_persons",
            joinColumns = @JoinColumn(name = "contact_phone"),
            inverseJoinColumns = @JoinColumn (name = "person"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"contact_phone", "person"})
    )
    private Set<Person> persons = new HashSet<>();

    private Boolean enabled;

}
