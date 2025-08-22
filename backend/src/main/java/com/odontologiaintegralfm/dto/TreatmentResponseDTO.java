package com.odontologiaintegralfm.dto;

import com.odontologiaintegralfm.model.TreatmentCondition;

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
