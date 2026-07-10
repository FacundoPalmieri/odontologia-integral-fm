package com.odontologiaintegralfm.feature.consultation.catalogs.promotion.mapper;

import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.dto.PromotionCreateRequestDTO;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.dto.PromotionResponseDTO;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.model.Promotion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PromotionMapper {

    PromotionResponseDTO toDTO(Promotion promotion);

    List<PromotionResponseDTO> toDTO(List<Promotion> promotions);

    @Mapping(target = "name", ignore = true)
    Promotion toEntity(PromotionCreateRequestDTO dto);
}