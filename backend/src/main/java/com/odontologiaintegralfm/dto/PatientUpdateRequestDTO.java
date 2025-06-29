package com.odontologiaintegralfm.dto;

import java.util.Set;

/**
 * DTO que representa la creación de un paciente.
 */
public record PatientUpdateRequestDTO(

        PersonUpdateRequestDTO person,
        Long healthPlanId,
        String affiliateNumber,
        Set<PatientMedicalRiskRequestDTO> medicalRisk
){}
