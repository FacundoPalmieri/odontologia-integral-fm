package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


/**
 * Entidad que representa a una Persona.
 */

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Audited
@Table(name = "persons", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"dni_type_id", "dni"})
})
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(length = 30, nullable = false)
    private String firstName;

    @Column(length = 30, nullable = false)
    private String lastName;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = DniType.class)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @JoinColumn(name = "dni_type_id")
    private DniType dniType;

    @Column(length = 15, nullable = false)
    private String dni;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Transient
    private int age;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Gender.class)
    @JoinColumn(name = "gender_id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Gender gender;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Nationality.class)
    @JoinColumn(name = "nationality_id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Nationality nationality;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "persons_contact_emails",
            joinColumns = @JoinColumn(name = "person_id"),
            inverseJoinColumns = @JoinColumn (name = "contact_email_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"person_id","contact_email_id"})
    )
    private Set<ContactEmail> contactEmails = new HashSet<>();


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "persons_contact_phones",
            joinColumns = @JoinColumn(name = "person_id"),
            inverseJoinColumns = @JoinColumn (name = "contact_phones_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"person_id", "contact_phones_id"})
    )
    private Set<ContactPhone> contactPhones = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Address.class)
    @JoinColumn(name = "address_id")
    private Address address;

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
