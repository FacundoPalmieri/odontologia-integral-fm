package com.odontologiaintegralfm.feature.consultation.core.odontogram.dto;


import com.odontologiaintegralfm.feature.consultation.core.odontogram.enums.Tooth;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.enums.ToothFace;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record OdontogramUpdateRequestDTO(

         // null = alta; con valor = identifica el registro persistido a editar/dar de baja (ADR-0008)
         Long id,

         @NotNull(message = "odontogramUpdateRequestDTO.tooth.empty")
         Tooth tooth,

         ToothFace toothFace,

         @NotNull(message = "odontogramUpdateRequestDTO.treatmentId.empty")
         Long treatmentId,

         @NotNull(message = "odontogramUpdateRequestDTO.treatmentConditionId.empty")
         Long treatmentConditionId,

         // true=vigente (alta o edición); false=baja explícita; siempre declarado, nunca implícito
         @NotNull(message = "odontogramUpdateRequestDTO.enabled.empty")
         Boolean enabled

){

    // Rechaza alta+baja combinada (id==null && enabled==false) — payload contradictorio, 400 (regla #7)
    @AssertTrue(message = "odontogramUpdateRequestDTO.notContradictory")
    public boolean isNotContradictory() {
        return !(id == null && Boolean.FALSE.equals(enabled));
    }
}