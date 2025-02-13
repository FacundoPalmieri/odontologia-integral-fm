package com.odontologiaintegralfm.exception;

import lombok.Getter;

/**
 * Excepción personalizada para indicar que un recurso no fue encontrado.
 * <p>
 * Se lanza cuando no existe un recurso con el ID especificado en la consulta.
 * </p>
 */
@Getter
public class ResourceNotFoundException extends RuntimeException {
    private Long id;

    public ResourceNotFoundException(String message, Long id) {
      super(message);
      this.id = id;
    }
}
