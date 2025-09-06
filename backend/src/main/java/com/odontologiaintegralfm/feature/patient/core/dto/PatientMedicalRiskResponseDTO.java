package com.odontologiaintegralfm.feature.patient.core.dto;

/**
 * @author [Facundo Palmieri]
 */
public record PatientMedicalRiskResponseDTO(
        Long id,
        String name,
        String observation
) {}
