package com.odontologiaintegralfm.dto;


import java.util.Set;


public record PatientResponseDTO(
        PersonResponseDTO person,
        String healthPlans,
        String affiliateNumber,
        Set<PatientMedicalRiskResponseDTO> medicalHistoryRisk
) {
}
