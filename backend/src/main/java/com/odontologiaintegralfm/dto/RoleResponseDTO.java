package com.odontologiaintegralfm.dto;

import lombok.Data;
import java.util.HashSet;
import java.util.Set;

@Data
public class RoleResponseDTO {

    private Long id;
    private String role;
    private Set<PermissionResponseDTO> permissionsList = new HashSet<>();
}
