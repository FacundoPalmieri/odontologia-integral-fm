package com.odontologiaintegralfm.feature.consultation.catalogs.treatment.mapper;

import com.odontologiaintegralfm.feature.consultation.catalogs.treatment.dto.TreatmentResponseDTO;
import com.odontologiaintegralfm.feature.consultation.catalogs.treatment.model.Treatment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TreatmentMapper {

    TreatmentResponseDTO toDTO(Treatment treatment);
}