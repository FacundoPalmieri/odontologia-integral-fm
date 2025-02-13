package com.odontologiaintegralfm.exception;

import lombok.Getter;
import lombok.Setter;


/**
 * Excepción lanzada cuando se intenta guardar un usuario con el rol de desarrollador.
 * <p>
 * Esta excepción extiende {@link RuntimeException} y se usa para indicar que una operación
 * de guardado no es válida debido a restricciones de rol.
 * </p>
 */
@Setter
@Getter
public class UserSaveNotDevRoleException extends RuntimeException {
    /** Tipo de la entidad (Ejemplo: "Usuario"). */
    private  String entityType;

    /** Operación realizada (Ejemplo: "Find", "Save", etc.). */
    private  String operation;

    /** ID del usuario no encontrado. */
    private Long id;

    public UserSaveNotDevRoleException(String message, String entityType, String operation, Long id) {
        super(message);
        this.entityType = entityType;
        this.operation = operation;
        this.id = id;
    }
}
