package com.odontologiaintegralfm.dto;



import jakarta.validation.constraints.NotNull;

public record FailedLoginAttemptsDTO(
        @NotNull(message = "FailedLoginAttemptsDTO.value.empty")
        Integer value) {
}
