package com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto;

public record PrestationStepResponseDTO(
        Long id,
        String nameStep,
        int position,
        boolean required
) {}