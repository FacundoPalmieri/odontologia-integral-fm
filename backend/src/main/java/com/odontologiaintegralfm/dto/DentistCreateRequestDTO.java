package com.odontologiaintegralfm.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DentistCreateRequestDTO(
        PersonCreateRequestDTO personDto,
        AddressRequestDTO addressDto,
        ContactRequestDTO contactDto,
        UserSecCreateDTO user,

        @NotBlank(message = "dentistCreateRequestDTO.licenseNumber.Empty")
        String licenseNumber,

        @NotNull(message = "dentistCreateRequestDTO.dentistSpecialtyId.Empty")
        Long dentistSpecialtyId
) {
}
