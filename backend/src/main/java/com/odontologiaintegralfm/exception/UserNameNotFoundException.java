package com.odontologiaintegralfm.exception;

import lombok.Getter;

/**
 * Excepción personalizada que indica que el nombre de usuario no se ha encontrado en el sistema al momento de autenticar.
 * <p>
 * Esta excepción se lanza cuando se intenta realizar una operación sobre un usuario que no existe en el sistema.
 * </p>
 */
@Getter
public class UserNameNotFoundException extends RuntimeException {
    /** Nombre de usuario que no se ha encontrado. */
    private String username;

    /** Constructor que inicializa la excepción */
    public UserNameNotFoundException(String username) {
        this.username = username;
    }
}
