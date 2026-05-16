package com.odontologiaintegralfm.feature.consultation.core.odontogram.dto;


import com.odontologiaintegralfm.feature.consultation.catalogs.enums.Tooth;
import com.odontologiaintegralfm.feature.consultation.catalogs.enums.ToothFace;
import jakarta.validation.constraints.NotNull;

public record OdontogramRequestDTO(

         @NotNull(message = "odontogramRequestDTO.tooth.empty")
         Tooth tooth,

         ToothFace toothFace,

         @NotNull(message = "odontogramRequestDTO.treatmentId.empty")
         Long treatmentId,

         @NotNull(message = "odontogramRequestDTO.treatmentConditionId.empty")
         Long treatmentConditionId

){}
