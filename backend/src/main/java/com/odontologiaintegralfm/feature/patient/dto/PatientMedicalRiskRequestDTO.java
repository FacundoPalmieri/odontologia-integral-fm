package com.odontologiaintegralfm.feature.patient.dto;

/**
 * DTO que se utiliza dentro de {@link PatientUpdateRequestDTO}
 */
public record PatientMedicalRiskRequestDTO(
        Long medicalRiskId,
        String observation
){

}
