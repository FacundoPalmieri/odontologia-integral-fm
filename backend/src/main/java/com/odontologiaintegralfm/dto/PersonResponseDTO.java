package com.odontologiaintegralfm.dto;

import java.time.LocalDate;


public record PersonResponseDTO(
        Long id,
        String firstName,
        String lastName,
        String dniType,
        String dni,
        LocalDate birthDate,
        int age,
        String gender,
        String nationality
) {
}
