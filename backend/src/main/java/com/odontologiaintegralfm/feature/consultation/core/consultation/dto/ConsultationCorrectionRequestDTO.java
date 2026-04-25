package com.odontologiaintegralfm.feature.consultation.core.consultation.dto;


import jakarta.validation.constraints.NotNull;

/**
 * DTO que contiene la observación que corresponde a una corrección de estado en una consulta.
 * Se revierte al estado anterior.
 */
public record ConsultationCorrectionRequestDTO(

        @NotNull(message = "consultationCorrectionRequestDTO.observation.empty")
        String observationCorrection
) {
}
