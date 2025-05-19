package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa los emails de contactos de las personas.
 */

@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "contacts_emails")
public class ContactEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Email(message = "contactEmail.formatError.user")
    @NotBlank(message = "contactEmail.empty.user")
    @Column(length = 50, nullable = false, unique = true)
    private String email;

    @ManyToMany(fetch = FetchType.LAZY, targetEntity = Person.class)
    @JoinTable(
            name = "contact_email_persons",
            joinColumns = @JoinColumn(name = "contact_email"),
            inverseJoinColumns = @JoinColumn (name = "person"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"contact_email", "person"})
    )
    private Set<Person> persons = new HashSet<>();

    private Boolean enabled;

}
