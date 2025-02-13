package com.odontologiaintegralfm.dto;

import jakarta.validation.constraints.NotNull;

public record TokenConfigDTO(@NotNull(message = "tokenDTO.expiration.empty") Long expiration) {
}
