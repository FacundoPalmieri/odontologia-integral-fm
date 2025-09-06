package com.odontologiaintegralfm.feature.person.catalogs.model;


import com.odontologiaintegralfm.shared.model.Auditable;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad que representa una provincia.
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name= "provinces")
public class Province extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(length = 15,unique=true,nullable = false)
    private String name;

    @ManyToOne(targetEntity = Country.class)
    @JoinColumn(name = "country_id")
    private Country country;

}
