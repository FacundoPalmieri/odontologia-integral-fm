package com.odontologiaintegralfm.dto;

import lombok.Data;
import java.util.HashSet;
import java.util.Set;

@Data
public class RoleFullResponseDTO {

    private Long id;
    private String name;
    private String label;
    private Set<PermissionFullResponseDTO> permissionsList = new HashSet<>();
}
