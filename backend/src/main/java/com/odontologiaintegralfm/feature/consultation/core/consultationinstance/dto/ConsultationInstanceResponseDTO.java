package com.odontologiaintegralfm.feature.consultation.core.consultationinstance.dto;

import com.odontologiaintegralfm.feature.consultation.core.odontogram.dto.OdontogramResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.prestation.dto.PrestationInstanceResponseDTO;

import java.util.List;

public record ConsultationInstanceResponseDTO(

        Long id,
        List<OdontogramResponseDTO> odontogram,
        List<PrestationInstanceResponseDTO> prestationInstance,
        String observation
) {}
