package com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto;

import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.enums.PrestationScopeType;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.enums.Maxillary;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.enums.Quadrant;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.enums.Tooth;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.enums.ToothFace;
import jakarta.validation.constraints.NotNull;

public record PrestationInstanceUpdateRequestDTO(

        // este DTO solo referencia PrestationInstance ya persistidas; las altas van en prestationNew
        @NotNull(message = "prestationInstanceUpdateRequestDTO.id.empty")
        Long id,

        // ubicación editable solo si la prestación NO tiene Odontogram asociado (validateLocationNotOverridable)
        PrestationScopeType scope,

        Tooth tooth,

        ToothFace toothFace,

        Quadrant quadrant,

        Maxillary maxillary,

        // true=sigue vigente (con o sin cambio de ubicación); false=baja explícita de la prestación completa
        @NotNull(message = "prestationInstanceUpdateRequestDTO.enabled.empty")
        Boolean enabled

) {
}