package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidad que representa configurar el tiempo de Expiración del Token.
 */
@Entity
@Data
@Table(name = "token_config")
public class TokenConfig {

    /**Identificador único del rol.*/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**Tiempo de expiración del Token.*/
    @Column(nullable = false)
    private Long expiration;
}
