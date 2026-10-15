package com.odontologiaintegralfm.feature.consultation.core.treatmentinstance.dto;

import jakarta.validation.constraints.AssertTrue;

/**
 * Referencia a un TreatmentInstance: por id si ya existe, por tempId si nace en el mismo request (D-23, D-38).
 */
public record TreatmentInstanceRefDTO(
        Long id,
        String tempId
) {

    @AssertTrue(message = "treatmentInstanceRefDTO.exactlyOneIdentifier")
    public boolean isExactlyOneIdentifier() {
        return (id != null) != (tempId != null);
    }
}
