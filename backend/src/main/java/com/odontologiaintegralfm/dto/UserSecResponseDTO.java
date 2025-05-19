package com.odontologiaintegralfm.dto;

import com.odontologiaintegralfm.model.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;


public record UserSecResponseDTO(
         Long id,
         String username,
         Set<Role> rolesList,
         boolean enabled
) {




}
