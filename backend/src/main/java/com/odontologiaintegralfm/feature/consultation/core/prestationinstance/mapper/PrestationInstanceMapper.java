package com.odontologiaintegralfm.feature.consultation.core.prestationinstance.mapper;

import com.odontologiaintegralfm.feature.consultation.core.odontogram.mapper.OdontogramMapper;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto.PrestationInstanceResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto.PrestationStepInstanceResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.model.PrestationInstance;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.model.PrestationStepInstance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = OdontogramMapper.class)
public interface PrestationInstanceMapper {

    // steps se ignora acá: PrestationInstance no tiene colección @OneToMany hacia sus steps —
    // el caller (UseCase) los busca vía IPrestationStepInstanceRepository y arma el DTO final con toStepDTO/toStepDTOs
    @Mapping(target = "name", source = "type.name")
    @Mapping(target = "promotion", source = "promotion.name")
    @Mapping(target = "steps", ignore = true)
    PrestationInstanceResponseDTO toDTO(PrestationInstance prestationInstance);

    default PrestationStepInstanceResponseDTO toStepDTO(PrestationStepInstance instance) {
        return new PrestationStepInstanceResponseDTO(
                instance.getId(),
                instance.getStep().getId(),
                instance.getStep().getStep().getName(),
                instance.getStep().getPosition(),
                instance.getStep().isRequired(),
                instance.getStatus().name()
        );
    }

    default java.util.List<PrestationStepInstanceResponseDTO> toStepDTOs(java.util.List<PrestationStepInstance> instances) {
        return instances.stream().map(this::toStepDTO).toList();
    }
}
