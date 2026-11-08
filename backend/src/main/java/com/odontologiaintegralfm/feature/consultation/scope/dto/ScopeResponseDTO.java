package com.odontologiaintegralfm.feature.consultation.scope.dto;

import com.odontologiaintegralfm.feature.consultation.scope.enums.Maxillary;
import com.odontologiaintegralfm.feature.consultation.scope.enums.Quadrant;
import com.odontologiaintegralfm.feature.consultation.scope.enums.Tooth;
import com.odontologiaintegralfm.feature.consultation.scope.enums.ToothFace;

import java.util.List;

/**
 * Ubicación clínica de la respuesta. Los grupos sin usar van en null:
 * tooth null si la lista está vacía y fullMouth null si es false (D-06).
 */
public record ScopeResponseDTO(
        List<Tooth> tooth,
        ToothFace toothFace,
        Quadrant quadrant,
        Maxillary maxillary,
        Boolean fullMouth
) {
}
