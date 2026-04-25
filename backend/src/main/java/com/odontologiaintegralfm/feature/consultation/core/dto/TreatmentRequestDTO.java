package com.odontologiaintegralfm.feature.consultation.core.dto;


import com.odontologiaintegralfm.feature.consultation.catalogs.enums.ToothFace;


/**
 * DTO que representa un tratamiento.
 */
public record TreatmentRequestDTO(
         Long idTreatment,
         Long idTreatmentCondition,
         ToothFace toothFace
) {
}
