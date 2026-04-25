package com.odontologiaintegralfm.feature.consultation.core.dto;

import com.odontologiaintegralfm.feature.consultation.catalogs.enums.ToothFace;


public record TreatmentResponseDTO(
        Long treatmentId,
        String treatmentName,
        String treatmentCondition,
        ToothFace toothFace
) {
}
