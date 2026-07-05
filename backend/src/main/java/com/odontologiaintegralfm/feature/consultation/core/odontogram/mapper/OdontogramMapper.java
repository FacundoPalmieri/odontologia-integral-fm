package com.odontologiaintegralfm.feature.consultation.core.odontogram.mapper;

import com.odontologiaintegralfm.feature.consultation.core.odontogram.dto.OdontogramResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.model.Odontogram;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OdontogramMapper {

    @Mapping(target = "treatmentName", source = "treatment.name")
    @Mapping(target = "treatmentLabel", source = "treatment.label")
    @Mapping(target = "treatmentCondition", source = "treatmentCondition.name")
    OdontogramResponseDTO toDTO(Odontogram odontogram);
}
