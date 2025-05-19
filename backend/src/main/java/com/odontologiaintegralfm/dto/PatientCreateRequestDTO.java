package com.odontologiaintegralfm.dto;

import java.util.Set;


public record PatientCreateRequestDTO(
        PersonCreateRequestDTO personDto,
        AddressRequestDTO addressDto,
        ContactRequestDTO contactDto,
        Long healthPlanId,
        String affiliateNumber,
        Set<MedicalHistoryRiskRequestDTO> medicalRiskDto
) {
}
