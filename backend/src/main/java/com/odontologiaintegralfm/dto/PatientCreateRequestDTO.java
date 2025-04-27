package com.odontologiaintegralfm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * DTO que representa la creación de un paciente.
 */
public record PatientCreateRequestDTO (

        //Persona
        @NotBlank(message = "PatientCreateRequestDTO.firstName.Empty")
        String firstName,

        @NotBlank(message = "PatientCreateRequestDTO.lastName.Empty")
        String lastName,

        @NotNull(message = "PatientCreateRequestDTO.dniTypeId.Empty")
        Long dniTypeId,

        @NotBlank(message = "PatientCreateRequestDTO.dni.Empty")
        String dni,

        @NotNull(message = "PatientCreateRequestDTO.birthDate.Empty")
        LocalDate birthDate,

        @NotNull(message = "PatientCreateRequestDTO.genderId.Empty")
        Long genderId,

        @NotNull(message = "PatientCreateRequestDTO.nationalityId.Empty")
        Long nationalityId,

        //Geografía.
        @NotNull(message = "PatientCreateRequestDTO.localityId.Empty")
        Long localityId,

        @NotBlank(message = "PatientCreateRequestDTO.street.Empty")
        String street,

        @NotNull(message = "PatientCreateRequestDTO.number.Empty")
        Integer number,

        String floor,

        String apartment,

        //Paciente- Planes de salud
        Long healthPlansId,

        String affiliateNumber,

        //Contacto
        @Email(message = "PatientCreateRequestDTO.email.Invalid")
        @NotBlank(message = "PatientCreateRequestDTO.email.Empty")
        String email,

        @NotBlank(message = "PatientCreateRequestDTO.phone.Empty")
        String phone

){}
