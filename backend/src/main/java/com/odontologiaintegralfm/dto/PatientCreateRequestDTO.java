package com.odontologiaintegralfm.dto;

import java.util.Set;


public record PatientCreateRequestDTO(
        PersonCreateRequestDTO person, //Incluye domicilio y Contactos
        Long healthPlanId,
        String affiliateNumber,
        Set<PatientMedicalRiskRequestDTO> medicalRisk
) {
}
