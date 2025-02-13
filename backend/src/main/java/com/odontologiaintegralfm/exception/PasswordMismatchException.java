package com.odontologiaintegralfm.exception;

import lombok.Getter;

/**
 * Excepción personalizada para manejar errores cuando las contraseñas no coinciden.
 * <p>
 * Se lanza cuando un usuario intenta establecer una contraseña que no coincide
 * con la esperada, ya sea al cambiarla o al crear una nueva cuenta.
 * </p>
 */
@Getter
public class PasswordMismatchException extends RuntimeException {

    /** Usuario que realizó la creación o modificación de la contraseña. */
    private String userCreador;

    /** Usuario cuya contraseña no coincide. */
    private String userNuevo;

    public PasswordMismatchException(String message, String userCreador, String userNuevo) {
        super(message);
        this.userCreador = userCreador;
        this.userNuevo = userNuevo;
    }

    public PasswordMismatchException(String userNuevo) {
        this.userNuevo = userNuevo;
    }
}

