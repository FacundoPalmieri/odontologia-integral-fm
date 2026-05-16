package com.odontologiaintegralfm.feature.consultation.core.odontogram.mapper;

import com.odontologiaintegralfm.feature.consultation.core.odontogram.dto.OdontogramResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.model.Odontogram;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OdontogramMapper {

    @Mapping(target = "treatment", source = "treatment.name")
    @Mapping(target = "treatmentCondition", source = "treatmentCondition.name")
    OdontogramResponseDTO toDTO(Odontogram odontogram);
}
