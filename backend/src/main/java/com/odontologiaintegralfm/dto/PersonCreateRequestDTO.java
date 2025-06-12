package com.odontologiaintegralfm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;

public record PersonCreateRequestDTO(
        //Persona
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
        Long nationalityId,

        @Email(message = "personCreateRequestDTO.email.Invalid")
        @NotBlank(message = "personCreateRequestDTO.email.Empty")
        Set<String> contactEmails,

        Set<ContactPhoneRequestDTO> contactPhones,

        AddressRequestDTO address

) {
}
