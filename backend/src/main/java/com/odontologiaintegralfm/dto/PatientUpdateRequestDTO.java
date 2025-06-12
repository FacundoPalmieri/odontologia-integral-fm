package com.odontologiaintegralfm.dto;

import java.util.Set;

/**
 * DTO que representa la creación de un paciente.
 */
public record PatientUpdateRequestDTO(

        PersonUpdateRequestDTO person,
        ContactPhoneRequestDTO contact,
        Long healthPlanId,
        String affiliateNumber,
        Set<PatientMedicalRiskRequestDTO> medicalRisk
){}
