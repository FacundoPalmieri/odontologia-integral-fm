package com.odontologiaintegralfm.feature.consultation.treatment.dto;

import com.odontologiaintegralfm.feature.consultation.treatment.enums.TreatmentConditionType;

import java.util.Set;

/**
 * @author [Facundo Palmieri]
 */
public record TreatmentResponseDTO(
        Long id,
        String label,
        String name,
        Set<TreatmentConditionType> conditions

) {
}
