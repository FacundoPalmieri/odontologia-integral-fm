package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * Entidad que representa los emails de contactos de las personas.
 */

@Entity
@Data
@Table(name = "contacts_emails")
public class ContactEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email(message = "contactEmail.formatError.user")
    @NotBlank(message = "contactEmail.empty.user")
    @Column(length = 50, nullable = false)
    private String email;

    @ManyToMany(targetEntity = Person.class)
    @JoinTable(
            name = "contact_email_people",
            joinColumns = @JoinColumn(name = "contact_email"),
            inverseJoinColumns = @JoinColumn (name = "people")
    )
    private List<Person> person;

    private Boolean enabled;

}
