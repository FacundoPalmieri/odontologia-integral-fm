package com.odontologiaintegralfm.dto;


public record DentistResponseDTO(
        PersonResponseDTO personDto,
        AddressResponseDTO addressDto,
        ContactResponseDTO contactDto,
        UserSecResponseDTO user,
        String licenseNumber,
        String dentistSpecialty
) {
}
