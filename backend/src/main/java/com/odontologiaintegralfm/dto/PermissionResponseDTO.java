package com.odontologiaintegralfm.dto;


import com.odontologiaintegralfm.model.Action;
import lombok.Data;

import java.util.Set;

@Data
public class PermissionResponseDTO {
    private Long id;
    private String permission;
    private String name;
    private Set<Action> actions;
}
