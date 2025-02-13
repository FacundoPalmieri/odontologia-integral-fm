package com.odontologiaintegralfm.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PermissionDTO {

    @NotNull(message = "permissionDTO.name.empty")
    private String permissionName;
}
