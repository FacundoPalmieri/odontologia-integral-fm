package com.odontologiaintegralfm.feature.consultation.core.odontogram.dto;

import com.odontologiaintegralfm.feature.consultation.core.odontogram.enums.Tooth;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.enums.ToothFace;

public record OdontogramResponseDTO(
        Long id,
        Tooth tooth,
        ToothFace toothFace,
        String treatmentName,
        String treatmentLabel,
        String treatmentCondition
) {}
