package com.odontologiaintegralfm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record ContactRequestDTO(
        @Email(message = "contactCreateRequestDTO.email.Invalid")
        @NotBlank(message = "contactCreateRequestDTO.email.Empty")
        String email,

        @NotNull(message = "contactCreateRequestDTO.phoneType.Empty")
        Long phoneType,

        @NotBlank(message = "contactCreateRequestDTO.phone.Empty")
        String phone
) {
}
