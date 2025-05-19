package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;


/**
 * Entidad que representa la configuración de intentos de inicio de sesión fallidos.
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "failed_login_config")
public class FailedLoginAttemptsConfig {

    /** Identificador único del mensaje.*/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * Valor que indica el número máximo de intentos fallidos permitidos.
     */
    @NotBlank
    @Column(nullable = false)
    private int value;
}
