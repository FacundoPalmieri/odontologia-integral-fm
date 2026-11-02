package com.odontologiaintegralfm.feature.consultation.prestation.mapper;

import com.odontologiaintegralfm.feature.consultation.prestation.dto.PrestationTypeResponseDTO;
import com.odontologiaintegralfm.feature.consultation.prestation.model.PrestationTypePrice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PrestationTypeMapper {

    @Mapping(target = "id", source = "prestationType.id")
    @Mapping(target = "name", source = "prestationType.name")
    @Mapping(target = "isUnique", source = "prestationType.unique")
    @Mapping(target = "label", source = "prestationType.label")
    @Mapping(target = "maxTooth", source = "prestationType.maxTooth")
    @Mapping(target = "requiresOriginTreatment", source = "prestationType.requiresOriginTreatment")
    @Mapping(target = "allowedScopes", source = "prestationType.allowedScopes")
    @Mapping(target = "currentPrice", source = "price")
    PrestationTypeResponseDTO toDTO(PrestationTypePrice ptp);

    List<PrestationTypeResponseDTO> toDTO(List<PrestationTypePrice> ptps);
}