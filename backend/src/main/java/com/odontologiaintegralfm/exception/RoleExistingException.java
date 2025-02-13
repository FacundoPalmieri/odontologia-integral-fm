package com.odontologiaintegralfm.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * Excepción personalizada para indicar que un rol ya existe en el sistema.
 * <p>
 * Se lanza cuando se intenta crear un rol que ya está registrado.
 * </p>
 */
@Getter
@Setter
public class RoleExistingException extends RuntimeException {

    /** Tipo de la entidad relacionada con la excepción (por ejemplo, "Usuario"). */
    private  String entityType;

    /** Operación que se intentó realizar (por ejemplo, "save", "Update"). */
    private  String operation;

    /** Nombre del rol que ya existe. */
    private String role;

    /** Constructor que inicializa la excepción*/
    public RoleExistingException(String message, String entityType, String operation, String role) {
        super(message);
        this.entityType = entityType;
        this.operation = operation;
        this.role = role;
    }
}
