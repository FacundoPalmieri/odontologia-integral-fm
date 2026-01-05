package com.odontologiaintegralfm.feature.consultation.core.dto;

import com.odontologiaintegralfm.feature.consultation.core.enums.Tooth;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * DTO que representa un diente con una lista de tratamientos asociados.
 */
public record ToothRequestDTO(
         @NotNull(message = "toothDTO.tooth.empty")
         Tooth tooth,

         @NotEmpty(message = "toothDTO.treatments.empty")
         List<TreatmentRequestDTO> treatments
) {
}
