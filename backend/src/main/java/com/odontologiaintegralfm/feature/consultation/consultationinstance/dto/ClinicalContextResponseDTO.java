package com.odontologiaintegralfm.feature.consultation.consultationinstance.dto;

import java.util.List;

/**
 * Contexto clínico de un paciente al iniciar una consulta (REQ-027): sus tratamientos abiertos y sus prestaciones
 * en curso, en dos listas planas.
 */
public record ClinicalContextResponseDTO(
        List<TreatmentInstanceContextResponseDTO> treatmentInstances,
        List<PrestationInstanceContextResponseDTO> prestationInstances
) {
}