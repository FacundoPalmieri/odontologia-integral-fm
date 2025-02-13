package com.odontologiaintegralfm.exception;

import lombok.Getter;
import lombok.Setter;


/**
 * Excepción lanzada cuando se intenta actualizar un usuario así mismo.
 * <p>
 * Esta excepción extiende {@link RuntimeException} y se usa para indicar que una operación
 * de actualización no es válida debido a restricciones de rol.
 * </p>
 */
@Getter
@Setter
public class UserUpdateSelfUpdateException extends RuntimeException {
  /** Tipo de la entidad (Ejemplo: "Usuario"). */
  private  String entityType;

  /** Operación realizada (Ejemplo: "Find", "Save", etc.). */
  private  String operation;

  /** ID del usuario no encontrado. */
  private Long id;

  public UserUpdateSelfUpdateException(String message, String entityType, String operation, Long id) {
    super(message);
    this.entityType = entityType;
    this.operation = operation;
    this.id = id;
  }
}
