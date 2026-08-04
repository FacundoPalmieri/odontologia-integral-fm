package com.odontologiaintegralfm.feature.consultation.core.consultationinstance.dto;


import com.odontologiaintegralfm.feature.consultation.core.odontogram.dto.OdontogramUpdateRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto.PrestationInstanceRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto.PrestationInstanceUpdateRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto.PrestationStepUpdateRequestDTO;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ConsultationInstanceUpdateRequestDTO(

        String observation,

        // estado completo deseado del odontograma tras la corrección
        @NotNull(message = "consultationInstanceUpdateRequestDTO.odontogram.empty")
        List<OdontogramUpdateRequestDTO> odontogram,

        // prestaciones persistidas referenciadas para edición de ubicación o baja
        @NotNull(message = "consultationInstanceUpdateRequestDTO.prestationExisting.empty")
        List<PrestationInstanceUpdateRequestDTO> prestationExisting,

        // steps sobre prestaciones persistidas: altas nuevas, avances o bajas individuales
        @NotNull(message = "consultationInstanceUpdateRequestDTO.stepAdvancements.empty")
        List<PrestationStepUpdateRequestDTO> stepAdvancements,

        // altas de prestaciones enteramente nuevas (mismo DTO que Create); sus steps van dentro de este elemento
        @NotNull(message = "consultationInstanceUpdateRequestDTO.prestationNew.empty")
        List<PrestationInstanceRequestDTO> prestationNew

) {
}