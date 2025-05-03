package com.odontologiaintegralfm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;

/**
 * DTO que representa la creación de un paciente.
 */
public record PatientCreateRequestDTO (

        //Persona
        @NotBlank(message = "patientCreateRequestDTO.firstName.Empty")
        String firstName,

        @NotBlank(message = "patientCreateRequestDTO.lastName.Empty")
        String lastName,

        @NotNull(message = "patientCreateRequestDTO.dniTypeId.Empty")
        Long dniTypeId,

        @NotBlank(message = "patientCreateRequestDTO.dni.Empty")
        String dni,

        @NotNull(message = "patientCreateRequestDTO.birthDate.Empty")
        LocalDate birthDate,

        @NotNull(message = "patientCreateRequestDTO.genderId.Empty")
        Long genderId,

        @NotNull(message = "patientCreateRequestDTO.nationalityId.Empty")
        Long nationalityId,

        //Geografía.
        @NotNull(message = "patientCreateRequestDTO.localityId.Empty")
        Long localityId,

        @NotBlank(message = "patientCreateRequestDTO.street.Empty")
        String street,

        @NotNull(message = "patientCreateRequestDTO.number.Empty")
        Integer number,

        String floor,

        String apartment,

        //Paciente- Planes de salud
        Long healthPlansId,

        String affiliateNumber,

        //Contacto
        @Email(message = "patientCreateRequestDTO.email.Invalid")
        @NotBlank(message = "patientCreateRequestDTO.email.Empty")
        String email,

        @NotNull(message = "patientCreateRequestDTO.phoneType.Empty")
        Long phoneType,

        @NotBlank(message = "patientCreateRequestDTO.phone.Empty")
        String phone,

        //Historia Clínica.
        Set<Long>medicalRiskId,

        String medicalHistoryObservation
){}
