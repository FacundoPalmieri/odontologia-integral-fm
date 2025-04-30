package com.odontologiaintegralfm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Set;

/**
 * @author [Facundo Palmieri]
 */
public record PatientCreateResponseDTO(
        Long id,
        String firstName,
        String lastName,
        String dniType,
        String dni,
        LocalDate birthDate,
        int age,
        String gender,
        String nationality,
        String locality,
        String street,
        Integer number,
        String floor,
        String apartment,
        String healthPlans,
        String affiliateNumber,
        String email,
        String phone,
        Set<MedicalRiskResponseDTO> medicalRisk,
        String medicalHistoryObservation

) {
}
