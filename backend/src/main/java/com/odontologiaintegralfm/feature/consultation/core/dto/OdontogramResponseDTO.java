package com.odontologiaintegralfm.feature.consultation.core.dto;

import com.odontologiaintegralfm.feature.consultation.core.model.OdontogramHeader;

import java.util.List;

/**
 * DTO que representa la respuesta de un odontograma.
 */
public record OdontogramResponseDTO(
        Long ConsultationId,
        Long OdontogramHeaderId,
        List<ToothResponseDTO> odontogram,
        String observation
) {

    public static OdontogramResponseDTO  build (OdontogramHeader header, List<ToothResponseDTO> odontogram) {
        return new OdontogramResponseDTO(
                header.getConsultation().getId(),
                header.getId(),
                odontogram,
                header.getObservation()
        );

    }
}
