package com.odontologiaintegralfm.dto;

import com.odontologiaintegralfm.model.Permission;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
public class RoleRequestDTO {

    @NotBlank(message = "roleDTO.role.empty")
    private String role;

    @NotNull(message = "roleDTO.permission.empty")
    private Set <Permission> permissionsList;
}
