package com.odontologiaintegralfm.feature.consultation.core.prestationinstance.mapper;

import com.odontologiaintegralfm.feature.consultation.core.odontogram.mapper.OdontogramMapper;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto.PrestationInstanceResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.model.PrestationInstance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = OdontogramMapper.class)
public interface PrestationInstanceMapper {

    @Mapping(target = "name", source = "type.name")
    @Mapping(target = "promotion", source = "promotion.name")
    PrestationInstanceResponseDTO toDTO(PrestationInstance prestationInstance);
}
