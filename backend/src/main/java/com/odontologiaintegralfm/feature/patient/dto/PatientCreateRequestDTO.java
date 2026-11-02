package com.odontologiaintegralfm.feature.patient.dto;

import com.odontologiaintegralfm.feature.person.person.dto.PersonCreateRequestDTO;

import java.util.Set;


public record PatientCreateRequestDTO(
        PersonCreateRequestDTO person, //Incluye domicilio y Contactos
        Long healthPlanId,
        String affiliateNumber,
        Set<PatientMedicalRiskRequestDTO> medicalRisk
) {
}
