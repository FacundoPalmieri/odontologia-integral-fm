package com.odontologiaintegralfm.shared.exception;

import lombok.Getter;
/**
 * Excepción personalizada para errores relacionados con la base de datos.
 * <p>
 * Se lanza cuando ocurre un error en la manipulación de entidades dentro de la base de datos.
 * </p>
 */
@Getter
public class DataBaseException extends RuntimeException {
    private final String clase;

    private final Long entityId;

    private final String entityName;

    private final String method;


     /** Constructor que inicializa la excepción con detalles de la entidad y la causa raíz del error.*/
    public DataBaseException(Throwable cause, String clase, Long entityId, String entityName, String method) {
        super(cause);
        this.clase = clase;
        this.entityId = entityId;
        this.entityName = entityName;
        this.method = method;
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