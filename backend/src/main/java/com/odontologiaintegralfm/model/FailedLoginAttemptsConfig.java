package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


/**
 * Entidad que representa la configuración de intentos de inicio de sesión fallidos.
 */
@Entity
@Data
@Table(name = "failed_login_config")
public class FailedLoginAttemptsConfig {

    /** Identificador único del mensaje.*/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Valor que indica el número máximo de intentos fallidos permitidos.
     */
    @NotBlank
    @Column(nullable = false)
    private int value;
}
