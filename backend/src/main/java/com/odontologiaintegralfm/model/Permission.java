package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Entidad que representa un permiso de usuario.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="permisos")
public class Permission {

    /**Identificador único del permiso.*/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    /**Nombre único del permiso.*/
    @Column(unique = true, nullable = false)
    private String permissionName;

}
