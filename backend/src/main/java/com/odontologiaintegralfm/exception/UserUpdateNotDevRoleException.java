package com.odontologiaintegralfm.exception;


import lombok.Getter;
import lombok.Setter;

/**
 * Excepción lanzada cuando se intenta actualizar un usuario con el rol de desarrollador o bien cuando se quiere actualizar un usuario con otro rol a desarrollador.
 * <p>
 * Esta excepción extiende {@link RuntimeException} y se usa para indicar que una operación
 * de actualización no es válida debido a restricciones de rol.
 * </p>
 */
@Getter
@Setter
public class UserUpdateNotDevRoleException extends RuntimeException {
    /** Tipo de la entidad (Ejemplo: "Usuario"). */
    private  String entityType;

    /** Operación realizada (Ejemplo: "Find", "Save", etc.). */
    private  String operation;

    /** ID del usuario no encontrado. */
    private Long id;

    public UserUpdateNotDevRoleException(String message, String entityType, String operation, Long id) {
        super(message);
        this.entityType = entityType;
        this.operation = operation;
        this.id = id;
    }
}
