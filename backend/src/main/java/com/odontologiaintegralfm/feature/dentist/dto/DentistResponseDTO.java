package com.odontologiaintegralfm.feature.dentist.dto;


import com.odontologiaintegralfm.feature.person.person.dto.PersonResponseDTO;

public record DentistResponseDTO(
        PersonResponseDTO person,
        String licenseNumber,
        DentistSpecialtyResponseDTO dentistSpecialty
) {
}
