package com.odontologiaintegralfm.feature.consultation.core.scope.dto;

import com.odontologiaintegralfm.feature.consultation.core.scope.enums.Maxillary;
import com.odontologiaintegralfm.feature.consultation.core.scope.enums.Quadrant;
import com.odontologiaintegralfm.feature.consultation.core.scope.enums.Tooth;
import com.odontologiaintegralfm.feature.consultation.core.scope.enums.ToothFace;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Ubicación clínica del request: a lo sumo un grupo poblado entre dientes (con o sin cara),
 * cuadrante, maxilar o boca completa (D-05, D-09, D-10).
 */
public record ScopeDTO(
        List<@NotNull(message = "scopeDTO.tooth.nullItem") Tooth> tooth,
        ToothFace toothFace,
        Quadrant quadrant,
        Maxillary maxillary,
        Boolean fullMouth
) {

    @AssertTrue(message = "scopeDTO.toothFaceWithTooth")
    public boolean isToothFaceWithTooth() {
        return toothFace == null || hasTooth();
    }

    @AssertTrue(message = "scopeDTO.singleLocationGroup")
    public boolean isSingleLocationGroup() {
        int groups = 0;
        if (hasTooth() || toothFace != null) groups++;
        if (quadrant != null) groups++;
        if (maxillary != null) groups++;
        if (Boolean.TRUE.equals(fullMouth)) groups++;
        return groups <= 1;
    }

    /** Helper, no es una validación: algún grupo de ubicación poblado (fullMouth = false equivale a null). */
    public boolean hasAnyLocation() {
        return hasTooth() || toothFace != null || quadrant != null || maxillary != null || Boolean.TRUE.equals(fullMouth);
    }

    private boolean hasTooth() {
        return tooth != null && !tooth.isEmpty();
    }
}
