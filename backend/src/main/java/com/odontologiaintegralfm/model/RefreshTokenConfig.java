package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidad que representa configurar el tiempo de Expiración del Refresh Token.
 */
@Entity
@Data
public class RefreshTokenConfig {

    /**Identificador único*/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**Tiempo de expiración del Refresh Token.*/
    @Column(name="Expiracion",  nullable = false)
    private Long expiration;
}