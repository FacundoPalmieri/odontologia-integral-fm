package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.envers.Audited;

/**
 * Entidad que los posibles Géneros de una persona.
 * Ej: Masculino - Feminino - No Binario.
 */
@Entity
@Data
@Table(name = "genders")
public class Gender {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Masculino - Feminino - No Binario
    @Column(length = 15, nullable = false, unique = true)
    private String name;

    // M - F - X
    @Column(length = 1, nullable = false, unique = true)
    private Character alias;

    private Boolean enabled;

}
