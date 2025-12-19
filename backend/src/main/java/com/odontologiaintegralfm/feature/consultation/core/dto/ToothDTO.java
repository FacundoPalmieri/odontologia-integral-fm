package com.odontologiaintegralfm.feature.consultation.core.dto;

import com.odontologiaintegralfm.feature.consultation.core.enums.Tooth;

import java.util.List;

/**
 * DTO que representa un diente con una lista de tratamientos asociados.
 */
public record ToothDTO(
         Tooth tooth,
         List<TreatmentRequestDTO> treatments
) {
}
