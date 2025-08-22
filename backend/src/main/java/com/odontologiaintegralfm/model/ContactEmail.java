package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 * Entidad que representa los emails de contactos de las personas.
 */

@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "contacts_emails")
@Audited
public class ContactEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Email(message = "contactEmail.formatError.user")
    @NotBlank(message = "contactEmail.empty.user")
    @Column(length = 50, nullable = false, unique = true)
    private String email;


}
