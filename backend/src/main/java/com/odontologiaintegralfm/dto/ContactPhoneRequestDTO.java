package com.odontologiaintegralfm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ContactPhoneRequestDTO(

        @NotNull(message = "ContactPhoneRequestDTO.phoneType.Empty")
        Long phoneType,

        @NotBlank(message = "ContactPhoneRequestDTO.phone.Empty")
        String phone
) {
}
