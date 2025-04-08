package com.odontologiaintegralfm.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PermissionRequestDTO {

    @NotNull(message = "permissionDTO.name.empty")
    private String permissionName;
}
