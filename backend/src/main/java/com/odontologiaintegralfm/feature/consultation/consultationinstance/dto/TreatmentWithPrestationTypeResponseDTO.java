package com.odontologiaintegralfm.feature.consultation.consultationinstance.dto;

import com.odontologiaintegralfm.feature.consultation.prestation.dto.PrestationTypeWithStepsResponseDTO;
import com.odontologiaintegralfm.feature.consultation.treatment.dto.TreatmentResponseDTO;

import java.util.List;

public record TreatmentWithPrestationTypeResponseDTO(
        TreatmentResponseDTO treatment,
        List<PrestationTypeWithStepsResponseDTO> prestationTypes
) {
}
