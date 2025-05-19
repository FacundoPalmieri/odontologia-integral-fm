package com.odontologiaintegralfm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;


public record PersonUpdateRequestDTO(
        Long id,

        @NotBlank(message = "personCreateRequestDTO.firstName.Empty")
        String firstName,

        @NotBlank(message = "personCreateRequestDTO.lastName.Empty")
        String lastName,

        @NotNull(message = "personCreateRequestDTO.dniTypeId.Empty")
        Long dniTypeId,

        @NotBlank(message = "personCreateRequestDTO.dni.Empty")
        String dni,

        @NotNull(message = "personCreateRequestDTO.birthDate.Empty")
        LocalDate birthDate,

        @NotNull(message = "personCreateRequestDTO.genderId.Empty")
        Long genderId,

        @NotNull(message = "personCreateRequestDTO.nationalityId.Empty")
        Long nationalityId

) {
}
