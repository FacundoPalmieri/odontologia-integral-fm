package com.odontologiaintegralfm.exception;

import lombok.Getter;
/**
 * Excepción personalizada para errores relacionados con la base de datos.
 * <p>
 * Se lanza cuando ocurre un error en la manipulación de entidades dentro de la base de datos.
 * </p>
 */
@Getter
public class DataBaseException extends RuntimeException {
    /** Tipo de la entidad afectada (Ejemplo: "UsuarioService"). */
    private final String entityType;

    /** ID de la entidad afectada. */
    private final Long entityId;

    /** Nombre descriptivo de la entidad afectada. */
    private final String entityName;

    /** Operación en la que ocurrió el error (Ejemplo: "Save", "Update"). */
    private final String operation;


     /** Constructor que inicializa la excepción con detalles de la entidad y la causa raíz del error.*/
    public DataBaseException(Throwable cause, String entityType, Long entityId, String entityName, String operation) {
        super(cause);
        this.entityType = entityType;
        this.entityId = entityId;
        this.entityName = entityName;
        this.operation = operation;
    }


    /**
     * Obtiene la causa raíz del error en forma de cadena.
     *
     * @return Descripción de la causa raíz o "Desconocida" si no hay información disponible.
     */
    public String getRootCause() {
        return getCause() != null ? getCause().toString() : "Desconocida";
    }

}