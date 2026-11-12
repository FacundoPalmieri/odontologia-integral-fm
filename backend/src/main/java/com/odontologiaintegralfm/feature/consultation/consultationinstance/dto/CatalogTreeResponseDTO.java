package com.odontologiaintegralfm.feature.consultation.consultationinstance.dto;

import com.odontologiaintegralfm.feature.consultation.prestation.dto.PrestationTypeWithStepsResponseDTO;

import java.util.List;

/**
 * Árbol clínico del odontólogo (REQ-039): rama 1 con tratamientos y sus prestaciones, rama 2 con las prestaciones sin tratamiento.
 */
public record CatalogTreeResponseDTO(
        List<TreatmentWithPrestationTypeResponseDTO> treatments,
        List<PrestationTypeWithStepsResponseDTO> prestationTypesWithoutTreatment
) {
}
