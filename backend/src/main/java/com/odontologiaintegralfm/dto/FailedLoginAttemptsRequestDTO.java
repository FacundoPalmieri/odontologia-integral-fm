package com.odontologiaintegralfm.dto;



import jakarta.validation.constraints.NotNull;

public record FailedLoginAttemptsRequestDTO(
        @NotNull(message = "FailedLoginAttemptsDTO.value.empty")
        Integer value) {
}
