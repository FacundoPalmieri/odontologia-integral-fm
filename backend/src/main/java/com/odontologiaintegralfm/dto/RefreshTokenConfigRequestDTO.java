package com.odontologiaintegralfm.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RefreshTokenConfigRequestDTO(
        @NotNull
        @Min(value = 1, message = "refreshTokenConfigRequestDTO.invalidExpiration")
        Long expiration
){}