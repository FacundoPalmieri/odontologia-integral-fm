package com.odontologiaintegralfm.dto;


import java.util.Set;


public record PatientResponseDTO(
        PersonResponseDTO personDto,
        String healthPlans,
        String affiliateNumber,
        Set<PatientMedicalRiskResponseDTO> medicalHistoryRiskDto
) {
}
