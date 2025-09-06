package com.odontologiaintegralfm.feature.patient.core.dto;

import com.odontologiaintegralfm.feature.person.core.dto.PersonUpdateRequestDTO;

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
