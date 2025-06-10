package com.odontologiaintegralfm.dto;


public record DentistResponseDTO(
        PersonResponseDTO personDto,
        String licenseNumber,
        String dentistSpecialty
) {
}
