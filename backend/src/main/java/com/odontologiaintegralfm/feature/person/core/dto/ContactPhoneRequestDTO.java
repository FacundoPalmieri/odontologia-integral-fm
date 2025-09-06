package com.odontologiaintegralfm.feature.person.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ContactPhoneRequestDTO(

        @NotNull(message = "contactPhoneRequestDTO.phoneType.Empty")
        Long phoneType,

        @NotBlank(message = "contactPhoneRequestDTO.phone.Empty")
        String phone
) {
}
