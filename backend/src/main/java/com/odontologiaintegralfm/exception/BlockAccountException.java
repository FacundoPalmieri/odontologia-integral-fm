package com.odontologiaintegralfm.exception;

import lombok.Getter;
/**
 * Excepción lanzada cuando una cuenta es bloqueada.
 * <p>Esta excepción es utilizada cuando una cuenta de usuario ha sido bloqueada debido a
 * intentos fallidos de inicio de sesión o cualquier otro motivo relacionado con la seguridad.</p>
 */
@Getter
public class BlockAccountException extends RuntimeException {
    private final Long id;
    private final String username;

    public BlockAccountException(String message, Long id, String username) {
        super(message);
        this.id = id;
        this.username = username;
    }
}
