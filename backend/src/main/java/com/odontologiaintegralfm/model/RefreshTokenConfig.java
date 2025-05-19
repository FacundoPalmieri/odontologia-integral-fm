package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad que representa configurar el tiempo de Expiración del Refresh Token.
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "refresh_token_config")
public class RefreshTokenConfig {

    /**Identificador único*/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**Tiempo de expiración del Refresh Token.*/
    @Column(nullable = false)
    private Long expiration;
}