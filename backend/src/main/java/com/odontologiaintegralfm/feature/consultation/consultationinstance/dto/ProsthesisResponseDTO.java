package com.odontologiaintegralfm.feature.consultation.consultationinstance.dto;

import com.odontologiaintegralfm.feature.consultation.scope.dto.ScopeDTO;

import java.util.List;

/**
 * Prótesis vigente de un paciente (REQ-038): el id de su tratamiento de origen y sus ubicaciones.
 * {@code scopes[0]} es el origen; el resto son los arreglos {@code FINISHED} que llevan dientes.
 */
public record ProsthesisResponseDTO(
        Long originTreatmentInstanceId,
        List<ScopeDTO> scopes
) {
}