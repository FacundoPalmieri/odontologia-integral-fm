package com.odontologiaintegralfm.feature.authentication.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;


/**
 * Entidad que representa un rol de usuario.
 */
@Audited
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "roles")
public class Role {

    /**Identificador único del rol.*/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**Nombre del rol.*/
    @Column(length = 50, unique = true)
    private String name;

    /**Nombre del rol.*/
    @Column(length = 50, unique = true)
    private String label;
}
