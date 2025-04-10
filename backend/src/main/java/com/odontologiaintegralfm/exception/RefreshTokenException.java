package com.odontologiaintegralfm.exception;


import lombok.Getter;

/**
 * Excepción personalizada para manejar errores de Refresh Token.
 * <p>
 * Se lanza cuando un refresh token no es encontrado, es inválido o está expirado.
 * </p>
 */
@Getter
public class RefreshTokenException extends RuntimeException {
    /** ID del mensaje no encontrado. */
    private Long id;

    /** Tipo de la entidad relacionada (Ejemplo: "UsuarioService"). */
    private  String entityType;

    /** Operación en la que ocurrió el error (Ejemplo: "Save"). */
    private  String operation;

    /** Mensaje con detalle. */
    private  String message;

    public RefreshTokenException(Long id, String entityType, String operation, String message)
    {
        this.id = id;
        this.entityType = entityType;
        this.operation = operation;
        this.message = message;
    }
}