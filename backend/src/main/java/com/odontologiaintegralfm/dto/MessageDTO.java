package com.odontologiaintegralfm.dto;

import jakarta.validation.constraints.NotNull;

public record MessageDTO(

        @NotNull
        Long id,

        @NotNull
        String key

) {
}
