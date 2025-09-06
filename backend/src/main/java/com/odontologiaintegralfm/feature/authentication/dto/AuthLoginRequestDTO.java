package com.odontologiaintegralfm.feature.authentication.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthLoginRequestDTO (@NotBlank String username, @NotBlank String password) {
}
