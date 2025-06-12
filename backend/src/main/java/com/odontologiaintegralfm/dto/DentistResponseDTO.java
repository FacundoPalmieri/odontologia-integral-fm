package com.odontologiaintegralfm.dto;


public record DentistResponseDTO(
        PersonResponseDTO person,
        String licenseNumber,
        String dentistSpecialty
) {
}
