package com.odontologiaintegralfm.feature.consultation.core.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * DTO que se utiliza para la creación de un odontograma.
 */
public record ConsultationOdontogramCreateRequestDTO(
        @NotEmpty(message = "OdontogramCreateRequestDTO.toothList.empty")
        List<ToothDTO> tooths,

        String observation

) {
}
