package com.odontologiaintegralfm.feature.patient.dto;

import com.odontologiaintegralfm.feature.person.person.dto.PersonUpdateRequestDTO;

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
