package com.odontologiaintegralfm.feature.dentist.core.dto;


import com.odontologiaintegralfm.feature.person.core.dto.PersonResponseDTO;

public record DentistResponseDTO(
        PersonResponseDTO person,
        String licenseNumber,
        String dentistSpecialty
) {
}
