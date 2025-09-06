package com.odontologiaintegralfm.feature.person.core.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddressRequestDTO(
        @NotNull(message = "addressRequestDTO.localityId.Empty")
        Long localityId,

        @NotBlank(message = "addressRequestDTO.street.Empty")
        String street,

        @NotNull(message = "addressRequestDTO.number.Empty")
        Integer number,

        String floor,

        String apartment
) {
}
