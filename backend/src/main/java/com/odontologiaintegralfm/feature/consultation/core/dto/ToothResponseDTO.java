package com.odontologiaintegralfm.feature.consultation.core.dto;


import com.odontologiaintegralfm.feature.consultation.catalogs.enums.Tooth;

import java.util.List;

public record ToothResponseDTO(
        Tooth tooth,
        List<TreatmentResponseDTO> treatments
) {
}
