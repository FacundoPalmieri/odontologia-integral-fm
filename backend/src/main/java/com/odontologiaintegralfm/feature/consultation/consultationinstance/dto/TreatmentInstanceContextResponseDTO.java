package com.odontologiaintegralfm.feature.consultation.consultationinstance.dto;

import com.odontologiaintegralfm.feature.consultation.scope.dto.ScopeResponseDTO;
import com.odontologiaintegralfm.feature.consultation.treatment.enums.TreatmentConditionType;
import com.odontologiaintegralfm.feature.dentist.dto.DentistSpecialtyResponseDTO;

import java.util.List;

public record TreatmentInstanceContextResponseDTO(
        Long id,
        String label,
        TreatmentConditionType treatmentCondition,
        ScopeResponseDTO scope,
        List<DentistSpecialtyResponseDTO> specialties
) {
}