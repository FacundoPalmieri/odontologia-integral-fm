package com.odontologiaintegralfm.feature.person.catalogs.model;

import com.odontologiaintegralfm.shared.model.Auditable;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad que los posibles Géneros de una persona.
 * Ej: Masculino - Feminino - No Binario.
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "genders")
public class Gender extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    //Masculino - Feminino - No Binario
    @Column(length = 15, nullable = false, unique = true)
    private String name;

    // M - F - X
    @Column(length = 1, nullable = false, unique = true)
    private Character alias;

}
