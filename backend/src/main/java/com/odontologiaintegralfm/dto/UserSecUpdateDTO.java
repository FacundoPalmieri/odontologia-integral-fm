package com.odontologiaintegralfm.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class UserSecUpdateDTO {

    @NotNull(message = "userSecCreateDTO.id.empty")
    private Long id;

    private Boolean enabled;
    private Set<Long> rolesList = new HashSet<>();




}
