package com.odontologiaintegralfm.dto;

/**
 * DTO que se utiliza dentro de {@link PatientUpdateRequestDTO}
 */
public record MedicalHistoryRiskRequestDTO(
        Long medicalRiskId,
        String observation
){

}
