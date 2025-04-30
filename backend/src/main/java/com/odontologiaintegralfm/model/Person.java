package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;


/**
 * Entidad que representa a una Persona.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Data
public abstract class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30, nullable = false)
    private String firstName;

    @Column(length = 30, nullable = false)
    private String lastName;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = DniType.class)
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
    private Gender gender;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Nationality.class)
    @JoinColumn(name = "nationality_id")
    private Nationality nationality;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Address.class)
    @JoinColumn(name = "address_id")
    private Address address;

    private LocalDate createdAt;

    private Boolean enabled;

    private LocalDate disabledAt;

}
