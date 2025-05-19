package com.odontologiaintegralfm.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddressRequestDTO(
        @NotNull(message = "addressCreateRequestDTO.localityId.Empty")
        Long localityId,

        @NotBlank(message = "addressCreateRequestDTO.street.Empty")
        String street,

        @NotNull(message = "addressCreateRequestDTO.number.Empty")
        Integer number,

        String floor,

        String apartment
) {
}
