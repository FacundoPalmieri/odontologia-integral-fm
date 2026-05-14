package com.odontologiaintegralfm.feature.consultation.core.odontogram.dto;

import com.odontologiaintegralfm.feature.consultation.catalogs.enums.Tooth;
import com.odontologiaintegralfm.feature.consultation.catalogs.enums.ToothFace;

public record OdontogramResponseDTO(
        Long id,
        Tooth tooth,
        ToothFace toothFace,
        String treatment,
        String treatmentCondition
) {}
