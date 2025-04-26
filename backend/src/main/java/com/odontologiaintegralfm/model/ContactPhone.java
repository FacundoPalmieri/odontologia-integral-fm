package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

/**
 * Entidad que representa los contactos telefónicos de las personas
 */

@Entity
@Data
@Table (name = "contacts_phones")
public class ContactPhone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(targetEntity = TelephoneType.class)
    @JoinColumn(name = "telephone_type_id")
    private TelephoneType telephoneType;

    @Column(length = 20, nullable = false)
    private String number;

    @ManyToMany(fetch = FetchType.LAZY, targetEntity = Person.class)
    @JoinTable(
            name = "contact_phone_people",
            joinColumns = @JoinColumn(name = "contact_phone"),
            inverseJoinColumns = @JoinColumn (name = "people")
    )
    private List<Person> person;

    private Boolean enabled;

}
