package com.odontologiaintegralfm.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * Excepción personalizada para manejar errores cuando no se encuentra un permiso requerido.
 * <p>
 * Se lanza cuando un usuario intenta realizar una operación para la cual no tiene permisos adecuados
 * o cuando la entidad a la que se intenta acceder no tiene los permisos configurados correctamente.
 * </p>
 */
@Getter
@Setter
public class PermissionNotFoundException extends RuntimeException {

    /** Identificador de la entidad asociada al permiso no encontrado. */
    private  Long id;

    /** Tipo de entidad a la que pertenece el permiso (Ejemplo: UsuarioService). */
    private  String entityType;

    /** Operación que intentó realizar el usuario (Ejemplo: Save). */
    private  String operation;

    /** Constructor que inicializa la excepción con un mensaje y detalles sobre la entidad y operación.*/
    public PermissionNotFoundException(String message,Long id, String entityType, String operation) {
        super(message);
        this.id = id;
        this.entityType = entityType;
        this.operation = operation;

    }
}
