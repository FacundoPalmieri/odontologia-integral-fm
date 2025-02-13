package com.odontologiaintegralfm.dto;

import com.odontologiaintegralfm.model.Role;
import lombok.Data;

import java.util.Set;

@Data
public class UserSecResponseDTO {

    private Long id;
    private String username;
    private Set<Role> rolesList;
    private boolean enabled;
}
