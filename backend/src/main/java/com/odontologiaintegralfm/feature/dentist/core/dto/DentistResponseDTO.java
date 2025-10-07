package com.odontologiaintegralfm.feature.dentist.core.dto;


import com.odontologiaintegralfm.feature.dentist.catalogs.dto.DentistSpecialtyResponseDTO;
import com.odontologiaintegralfm.feature.person.core.dto.PersonResponseDTO;

public record DentistResponseDTO(
        PersonResponseDTO person,
        String licenseNumber,
        DentistSpecialtyResponseDTO dentistSpecialty
) {
}
