package com.odontologiaintegralfm.dto;

import java.util.Set;

/**
 * DTO que representa la creación de un paciente.
 */
public record PatientUpdateRequestDTO(

        PersonUpdateRequestDTO personDto,
        AddressRequestDTO addressDto,
        ContactRequestDTO contactDto,
        Long healthPlanId,
        String affiliateNumber,
        Set<MedicalHistoryRiskRequestDTO>medicalRiskDto
){}
