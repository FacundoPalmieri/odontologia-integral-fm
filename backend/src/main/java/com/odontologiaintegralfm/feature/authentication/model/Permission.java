package com.odontologiaintegralfm.feature.authentication.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;


/**
 * Entidad que representa un permiso de usuario.
 */
@Audited
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "permissions")
public class Permission {

    /**Identificador único del permiso.*/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**Descripción del permiso.*/
    @Column(length = 50, unique = true, nullable = false)
    private String name;

    /**Nombre del permiso.*/
    @Column(length = 50, unique = true, nullable = false)
    private String label;



}
