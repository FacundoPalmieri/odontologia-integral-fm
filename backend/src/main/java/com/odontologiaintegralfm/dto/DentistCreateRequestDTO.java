package com.odontologiaintegralfm.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DentistCreateRequestDTO(
        @NotBlank(message = "dentistCreateRequestDTO.licenseNumber.Empty")
        String licenseNumber,

        @NotNull(message = "dentistCreateRequestDTO.dentistSpecialtyId.Empty")
        Long dentistSpecialtyId
) {
}
