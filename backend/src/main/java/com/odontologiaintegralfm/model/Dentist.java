package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidad que representa los Odontólogos
 */
@Entity
@Data
@Table(name ="dentists")
public class Dentist extends Person {

    @Column(length = 30, unique = true, nullable = false)
    private String licenseNumber;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = DentistSpecialty.class)
    @JoinColumn(name = "dentist_specialty_id")
    private DentistSpecialty dentistSpecialty;

    @OneToOne(fetch = FetchType.LAZY, targetEntity = UserSec.class)
    @JoinColumn(name = "user_id")
    private UserSec user;

}
