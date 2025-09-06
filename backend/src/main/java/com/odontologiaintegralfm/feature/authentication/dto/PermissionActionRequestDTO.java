package com.odontologiaintegralfm.feature.authentication.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.Set;

/**
 * DTO que se utiliza como atributo del RoleRequestDTO para la creación de roles.
 */
@Getter
@Setter
public class PermissionActionRequestDTO {
    private Long permissionId;
    private Set<Long> actionId;
}
