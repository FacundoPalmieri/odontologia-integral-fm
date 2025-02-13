package com.odontologiaintegralfm.exception;

import lombok.Getter;
import lombok.Setter;


/**
 * Excepción lanzada cuando ocurre un error al actualizar un usuario.
 * <p>
 * Esta excepción extiende {@link RuntimeException} y se usa para indicar
 * que una operación de actualización no pudo completarse correctamente.
 * </p>
 */
@Getter
@Setter
public class UserUpdateException extends RuntimeException {
    /** Tipo de la entidad (Ejemplo: "Usuario"). */
    private  String entityType;

    /** Operación realizada (Ejemplo: "Find", "Save", etc.). */
    private  String operation;

    /** ID del usuario no encontrado. */
    private Long id;

    public UserUpdateException(String message, String entityType, String operation, Long id) {
        super(message);
        this.entityType = entityType;
        this.operation = operation;
        this.id = id;
    }
}
