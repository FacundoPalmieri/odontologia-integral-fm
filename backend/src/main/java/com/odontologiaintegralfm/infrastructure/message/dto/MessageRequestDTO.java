package com.odontologiaintegralfm.infrastructure.message.dto;

import jakarta.validation.constraints.NotNull;

public record MessageRequestDTO(

        @NotNull
        Long id,

        @NotNull
        String value

) {
}
