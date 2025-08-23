package com.odontologiaintegralfm.feature.authentication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.Set;

@Data
public class RoleRequestDTO {

    @NotBlank(message = "roleDTO.role.empty")
    private String name;

    @NotBlank(message = "roleDTO.role.empty")
    private String label;

    @NotNull(message = "roleDTO.permission.empty")
    private Set <PermissionActionRequestDTO> permissionsList;
}
