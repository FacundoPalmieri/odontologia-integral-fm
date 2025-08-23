package com.odontologiaintegralfm.feature.person.core.model;


import com.odontologiaintegralfm.feature.person.catalogs.model.DniType;
import com.odontologiaintegralfm.feature.person.catalogs.model.Gender;
import com.odontologiaintegralfm.feature.person.catalogs.model.Nationality;
import com.odontologiaintegralfm.shared.model.Auditable;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;


/**
 * Entidad que representa a una Persona.
 */

@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Audited
@Table(name = "persons", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"dni_type_id", "dni"})
})
public class Person extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

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

}
