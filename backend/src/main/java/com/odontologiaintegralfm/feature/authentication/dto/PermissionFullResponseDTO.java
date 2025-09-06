package com.odontologiaintegralfm.feature.authentication.dto;



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
    private String name;
    private String label;
    private Set<ActionResponseDTO> actions;
}
