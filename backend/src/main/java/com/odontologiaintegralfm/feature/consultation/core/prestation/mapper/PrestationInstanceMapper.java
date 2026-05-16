package com.odontologiaintegralfm.feature.consultation.core.prestation.mapper;

import com.odontologiaintegralfm.feature.consultation.core.odontogram.mapper.OdontogramMapper;
import com.odontologiaintegralfm.feature.consultation.core.prestation.dto.PrestationInstanceResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.prestation.model.PrestationInstance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = OdontogramMapper.class)
public interface PrestationInstanceMapper {

    @Mapping(target = "name", source = "type.name")
    @Mapping(target = "promotion", source = "promotion.name")
    PrestationInstanceResponseDTO toDTO(PrestationInstance prestationInstance);
}
