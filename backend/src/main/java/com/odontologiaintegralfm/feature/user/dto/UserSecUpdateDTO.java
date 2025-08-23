package com.odontologiaintegralfm.feature.user.dto;

import com.odontologiaintegralfm.feature.dentist.core.dto.DentistUpdateRequestDTO;
import com.odontologiaintegralfm.feature.person.core.dto.PersonUpdateRequestDTO;
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
    private PersonUpdateRequestDTO person;
    //Opcional
    private DentistUpdateRequestDTO dentist;
}
