package com.odontologiaintegralfm.dto;

import jakarta.validation.constraints.NotNull;

public record MessageRequestDTO(

        @NotNull
        Long id,

        @NotNull
        String value

) {
}
