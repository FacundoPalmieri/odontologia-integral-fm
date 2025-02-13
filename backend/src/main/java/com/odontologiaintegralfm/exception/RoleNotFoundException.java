package com.odontologiaintegralfm.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * Excepción personalizada para indicar que un rol no fue encontrado en el sistema.
 * <p>
 * Se lanza cuando se intenta acceder o modificar un rol que no existe.
 * </p>
 */
@Getter
@Setter
public class RoleNotFoundException extends RuntimeException {

    /** Identificador único del rol que no se encontró. */
    private Long id;

    /** Nombre del rol que no fue encontrado. */
    private String role;

    /** Tipo de la entidad relacionada con la excepción (por ejemplo, "UsuarioService"). */
    private  String entityType;

    /** Operación que se intentó realizar (por ejemplo, "Save"). */
    private  String operation;

    /** Constructor que inicializa la excepción*/
    public RoleNotFoundException(String message, Long id, String role, String entityType, String operation) {
        super(message);
        this.id = id;
        this.role = role;
        this.entityType = entityType;
        this.operation = operation;
    }
}
