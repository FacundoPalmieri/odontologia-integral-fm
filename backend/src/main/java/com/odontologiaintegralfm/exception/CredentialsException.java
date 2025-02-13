package com.odontologiaintegralfm.exception;

import lombok.Getter;

/**
 * Excepción lanzada cuando hay un problema con las credenciales del usuario.
 * <p>Esta excepción se utiliza cuando las credenciales proporcionadas por el usuario
 * (como el nombre de usuario o la contraseña) son incorrectas o no válidas.</p>
 */
@Getter
public class CredentialsException extends RuntimeException {

    private final String username;


    public CredentialsException(String username) {
        this.username = username;
    }
}