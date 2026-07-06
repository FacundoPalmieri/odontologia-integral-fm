package com.odontologiaintegralfm.feature.consultation.core.prestationinstance.mapper;

import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.model.PrestationStep;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto.PrestationStepResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PrestationStepMapper {

    @Mapping(source = "step.name", target = "nameStep")
    PrestationStepResponseDTO toDTO(PrestationStep prestationStep);

    List<PrestationStepResponseDTO> toDTO(List<PrestationStep> prestationSteps);
}