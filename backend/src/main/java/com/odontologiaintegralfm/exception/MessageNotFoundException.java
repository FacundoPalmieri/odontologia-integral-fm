package com.odontologiaintegralfm.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * Excepción personalizada para manejar errores cuando un mensaje no es encontrado.
 * <p>
 * Se lanza cuando un mensaje asociado a una entidad específica no se encuentra en el sistema.
 * </p>
 */
@Getter
@Setter
public class MessageNotFoundException extends RuntimeException {
    /** ID del mensaje no encontrado. */
    private Long id;

    /** Tipo de la entidad relacionada (Ejemplo: "UsuarioService"). */
    private  String entityType;

    /** Operación en la que ocurrió el error (Ejemplo: "Save"). */
    private  String operation;

    /** Constructor que inicializa la excepción con detalles del mensaje no encontrado.*/
    public MessageNotFoundException(String message,Long id,  String entityType, String operation) {
        super(message);
        this.id = id;
        this.entityType = entityType;
        this.operation = operation;

    }
}
