package com.odontologiaintegralfm.feature.patient.core.dto;

import com.odontologiaintegralfm.feature.person.core.dto.PersonCreateRequestDTO;

import java.util.Set;


public record PatientCreateRequestDTO(
        PersonCreateRequestDTO person, //Incluye domicilio y Contactos
        Long healthPlanId,
        String affiliateNumber,
        Set<PatientMedicalRiskRequestDTO> medicalRisk
) {
}
