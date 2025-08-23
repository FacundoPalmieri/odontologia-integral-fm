package com.odontologiaintegralfm.feature.consultation.catalogs.dto;

import com.odontologiaintegralfm.feature.consultation.catalogs.model.TreatmentCondition;

import java.util.Set;

/**
 * @author [Facundo Palmieri]
 */
public record TreatmentResponseDTO(
        Long id,
        String name,
        Set<TreatmentCondition> conditions

) {
}
