package com.odontologiaintegralfm.exception;

import lombok.Getter;

/**
 * Excepción personalizada para indicar que un token de autenticación es inválido.
 * <p>
 * Se lanza cuando un usuario intenta autenticarse o realizar una acción con un token que es inválido,
 * ya sea porque está expirado, malformado o no autorizado.
 * </p>
 */
@Getter
public class TokenInvalidException extends RuntimeException {
    /** Nombre de usuario asociado al token inválido. */
    private final String username;

    /** Constructor que inicializa la excepción */
    public TokenInvalidException(String message, String username) {
        super(message);
        this.username = username;
    }
}
