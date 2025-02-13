package com.odontologiaintegralfm.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * Excepción personalizada para indicar que un rol no fue encontrado durante la creación de un usuario.
 * <p>
 * Se lanza cuando se intenta asignar un rol a un nuevo usuario, pero el rol especificado no existe en el sistema.
 * </p>
 */
@Getter
@Setter
public class RoleNotFoundUserCreationException extends RuntimeException {

  /** Identificador del usuario relacionado con la excepción. */
  private Long id;

  /** Nombre del rol que no fue encontrado. */
  private String role;

  /** Tipo de la entidad relacionada con la excepción (por ejemplo, "UsuarioService"). */
  private  String entityType;

  /** Operación que se intentó realizar (por ejemplo, "Save"). */
  private  String operation;


  /** Constructor que inicializa la excepción*/
  public RoleNotFoundUserCreationException(String message, Long id, String role, String entityType, String operation) {
    super(message);
    this.id = id;
    this.role = role;
    this.entityType = entityType;
    this.operation = operation;
  }
}
