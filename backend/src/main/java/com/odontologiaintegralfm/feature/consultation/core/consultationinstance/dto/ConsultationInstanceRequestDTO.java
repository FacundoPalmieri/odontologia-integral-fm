package com.odontologiaintegralfm.feature.consultation.core.consultationinstance.dto;


import com.odontologiaintegralfm.feature.consultation.core.odontogram.dto.OdontogramRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto.PrestationInstanceRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto.PrestationStepAdvancementRequestDTO;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ConsultationInstanceRequestDTO(

        @NotNull(message = "consultationInstanceRequestDTO.id.empty")
        Long consultationId,

        String observation,

        // cambios del odontograma en esta visita
        @NotNull(message = "consultationInstanceRequestDTO.odontogram.empty")
        List<OdontogramRequestDTO> odontogram,

        // prestaciones nuevas
        @NotEmpty(message = "consultationInstanceRequestDTO.prestationNew.empty")
        List<PrestationInstanceRequestDTO> prestationNew,

        // avances de prestaciones existentes
        @NotNull(message = "consultationInstanceRequestDTO.stepAdvancements.empty")
        List<PrestationStepAdvancementRequestDTO> stepAdvancements

) {
}