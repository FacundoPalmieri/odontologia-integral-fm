package com.odontologiaintegralfm.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class UserSecUpdateDTO {

    @NotNull(message = "generic.id.empty")
    private Long id;

    @NotNull(message = "userSecUpdateDTO.enabled.Empty")
    private Boolean enabled;

    @NotEmpty (message = "userSecUpdateDTO.rolesList.Empty")
    private Set<Long> rolesList = new HashSet<>();

    //Opcional
    private PersonUpdateRequestDTO personUpdateRequestDTO;
    //Opcional
    private DentistUpdateRequestDTO dentistUpdateRequestDTO;
}
