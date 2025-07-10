package com.odontologiaintegralfm.dto;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResponseDTO {
    private Long id;
    private String permission;
    private String name;
    private Set<ActionResponseDTO> actions;
}
