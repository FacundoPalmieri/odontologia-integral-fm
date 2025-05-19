package com.odontologiaintegralfm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record ContactRequestDTO(
        @Email(message = "contactRequestDTO.email.Invalid")
        @NotBlank(message = "contactRequestDTO.email.Empty")
        String email,

        @NotNull(message = "contactRequestDTO.phoneType.Empty")
        Long phoneType,

        @NotBlank(message = "contactRequestDTO.phone.Empty")
        String phone
) {
}
