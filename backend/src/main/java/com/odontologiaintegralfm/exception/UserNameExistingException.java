package com.odontologiaintegralfm.exception;


import lombok.Getter;
import lombok.Setter;

/**
 * Excepción personalizada que indica que el nombre de usuario ya existe en el sistema.
 * <p>
 * Esta excepción se lanza cuando se intenta crear una entidad con un nombre de usuario que ya está registrado en el sistema.
 * </p>
 */
@Setter
@Getter
public class UserNameExistingException extends RuntimeException {

    /** Tipo de la entidad (por ejemplo, CursosService). */
    private  String entityType;

    /** Operación realizada (por ejemplo, Save, Update). */
    private  String operation;

    /** Nombre de usuario que ya existe. */
    private String username;


    /** Constructor que inicializa la excepción */
    public UserNameExistingException( String username, String entityType, String operation) {
        this.username = username;
        this.entityType = entityType;
        this.operation = operation;
    }
}
