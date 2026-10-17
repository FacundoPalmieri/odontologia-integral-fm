package com.odontologiaintegralfm.feature.consultation.treatment.mapper;

import com.odontologiaintegralfm.feature.consultation.treatment.dto.TreatmentResponseDTO;
import com.odontologiaintegralfm.feature.consultation.treatment.model.Treatment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TreatmentMapper {

    TreatmentResponseDTO toDTO(Treatment treatment);
}