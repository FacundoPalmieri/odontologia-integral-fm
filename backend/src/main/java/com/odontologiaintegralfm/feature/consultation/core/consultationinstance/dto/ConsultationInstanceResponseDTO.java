package com.odontologiaintegralfm.feature.consultation.core.consultationinstance.dto;

import com.odontologiaintegralfm.feature.consultation.core.consultation.dto.ConsultationResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.dto.OdontogramResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto.PrestationInstanceResponseDTO;

import java.math.BigDecimal;
import java.util.List;

public record ConsultationInstanceResponseDTO(

        Long id,
        ConsultationResponseDTO consultationResponseDTO,
        List<OdontogramResponseDTO> odontogram,
        List<PrestationInstanceResponseDTO> prestationInstance,
        String observation,

        BigDecimal totalFinalAmount
) {}
