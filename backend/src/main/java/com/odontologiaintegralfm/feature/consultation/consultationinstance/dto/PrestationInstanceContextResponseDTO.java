package com.odontologiaintegralfm.feature.consultation.consultationinstance.dto;

import com.odontologiaintegralfm.feature.consultation.prestationinstance.enums.PrestationInstanceStatus;
import com.odontologiaintegralfm.feature.consultation.scope.dto.ScopeDTO;
import com.odontologiaintegralfm.feature.dentist.dto.DentistSpecialtyResponseDTO;

import java.util.List;

public record PrestationInstanceContextResponseDTO(
        Long id,
        String label,
        PrestationInstanceStatus status,
        ScopeDTO scope,
        Long treatmentInstanceId,
        Long originTreatmentInstanceId,
        List<ScopeDTO> originScopes,
        List<DentistSpecialtyResponseDTO> specialties,
        List<StepContextResponseDTO> steps
) {
}