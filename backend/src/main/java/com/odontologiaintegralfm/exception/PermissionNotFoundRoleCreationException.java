package com.odontologiaintegralfm.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * Excepción personalizada para manejar errores cuando no se encuentra un permiso durante la creación de un rol.
 * <p>
 * Se lanza cuando un usuario intenta crear un rol y no tiene los permisos adecuados o
 * cuando no se encuentra la configuración de permisos necesaria para dicha operación.
 * </p>
 */
@Getter
@Setter
public class PermissionNotFoundRoleCreationException extends RuntimeException {
    /** Identificador de la entidad asociada al permiso no encontrado. */
    private  Long id;

    /** Tipo de entidad a la que pertenece el permiso (Ejemplo: UsuarioService). */
    private  String entityType;

    /** Operación que intentó realizar el usuario (Ejemplo: Save, Update). */
    private  String operation;

    /** Constructor que inicializa la excepción con un mensaje y detalles sobre la entidad y operación.*/
    public PermissionNotFoundRoleCreationException(String message, Long id, String entityType, String operation) {
        super(message);
        this.id = id;
        this.entityType = entityType;
        this.operation = operation;
    }
}
