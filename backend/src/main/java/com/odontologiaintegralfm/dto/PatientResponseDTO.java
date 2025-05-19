package com.odontologiaintegralfm.dto;


import java.util.Set;


public record PatientResponseDTO(
        PersonResponseDTO personDto,
        AddressResponseDTO addressDto,
        ContactResponseDTO contactDto,
        String healthPlans,
        String affiliateNumber,
        Long medicalHistory,
        Set<MedicalHistoryRiskResponseDTO> medicalHistoryRiskDto
) {
}
