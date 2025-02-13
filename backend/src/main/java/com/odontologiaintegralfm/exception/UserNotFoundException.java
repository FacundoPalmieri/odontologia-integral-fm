package com.odontologiaintegralfm.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * Excepción personalizada que indica que no se ha encontrado un usuario en el sistema en búsquedas internas ajenas a la autenticación.
 * <p>
 * Esta excepción se utiliza generalmente en métodos como {@code findById}, donde se espera que un recurso
 * (en este caso, un usuario) sea encontrado, pero no se encuentra en la base de datos. El código de estado
 * asociado con esta excepción es un 404 (Not Found).
 * </p>
 */
@Getter
@Setter
public class UserNotFoundException extends RuntimeException {

  /** Tipo de la entidad (Ejemplo: "Usuario"). */
  private  String entityType;

  /** Operación realizada (Ejemplo: "Find", "Save", etc.). */
  private  String operation;

  /** ID del usuario no encontrado. */
  private Long id;

  /** Constructor que inicializa la excepción */
  public UserNotFoundException(String message,String entityType,String operation,Long id) {

    super(message);
    this.entityType = entityType;
    this.operation = operation;
    this.id = id;

  }
}
