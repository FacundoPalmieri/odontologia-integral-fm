package com.odontologiaintegralfm.dto;

/**
 * @author [Facundo Palmieri]
 */
public record PatientMedicalRiskResponseDTO(
        Long id,
        String name,
        String observation
) {}
