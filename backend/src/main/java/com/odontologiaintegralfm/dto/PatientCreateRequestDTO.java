package com.odontologiaintegralfm.dto;

import java.util.Set;

/**
 * @author [Facundo Palmieri]
 */
public record PatientCreateRequestDTO(
        PersonCreateRequestDTO personDto,
        AddressRequestDTO addressDto,
        ContactRequestDTO contactDto,
        Long healthPlanId,
        String affiliateNumber,
        Set<MedicalHistoryRiskRequestDTO> medicalRiskDto
) {
}
