package com.odontologiaintegralfm.feature.consultation.prestation.dto;

public record PrestationStepResponseDTO(
        Long id,
        String name,
        String label,
        boolean required,
        int position
) {
}
