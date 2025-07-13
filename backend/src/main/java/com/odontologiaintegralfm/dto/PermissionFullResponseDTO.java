package com.odontologiaintegralfm.dto;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO que se utiliza como atributo de RoleFullResponseDTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionFullResponseDTO {
    private Long id;
    private String permission;
    private String name;
    private Set<ActionResponseDTO> actions;
}
