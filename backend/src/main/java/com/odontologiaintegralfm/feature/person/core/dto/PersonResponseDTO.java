package com.odontologiaintegralfm.feature.person.core.dto;

import java.time.LocalDate;
import java.util.Set;


public record PersonResponseDTO(
        Long id,
        String firstName,
        String lastName,
        String dniType,
        String dni,
        LocalDate birthDate,
        int age,
        String gender,
        String nationality,
        Set<String> contactEmails,
        Set<ContactPhoneResponseDTO> contactPhone,
        AddressResponseDTO address
) {
}
