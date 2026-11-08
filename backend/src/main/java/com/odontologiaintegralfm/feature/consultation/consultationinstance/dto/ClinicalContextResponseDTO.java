package com.odontologiaintegralfm.feature.consultation.consultationinstance.dto;

import com.odontologiaintegralfm.feature.consultation.prestationinstance.dto.PrestationInstanceContextResponseDTO;
import com.odontologiaintegralfm.feature.consultation.treatmentinstance.dto.TreatmentInstanceContextResponseDTO;

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