package com.odontologiaintegralfm.feature.consultation.catalogs.prestation.mapper;

import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.dto.PrestationTypeResponseDTO;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.model.PrestationTypePrice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PrestationTypeMapper {

    @Mapping(target = "id", source = "prestationType.id")
    @Mapping(target = "name", source = "prestationType.name")
    @Mapping(target = "isUnique", source = "prestationType.unique")
    @Mapping(target = "hasSteps", source = "prestationType.hasSteps")
    @Mapping(target = "requiresLocation", source = "prestationType.requiresLocation")
    @Mapping(target = "allowedScopes", source = "prestationType.allowedScopes")
    @Mapping(target = "currentPrice", source = "price")
    PrestationTypeResponseDTO toDTO(PrestationTypePrice ptp);

    List<PrestationTypeResponseDTO> toDTO(List<PrestationTypePrice> ptps);
}