package com.odontologiaintegralfm.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RefreshTokenConfigDTO(
        @NotNull
        @Min(value = 1, message = "refreshTokenConfigDTO.invalidExpiration")
        Long expiration
){}