package com.odontologiaintegralfm.feature.consultation.scope.mapper;

import com.odontologiaintegralfm.feature.consultation.scope.dto.ScopeDTO;
import com.odontologiaintegralfm.feature.consultation.scope.model.Scope;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ScopeMapper {

    /**
     * Convierte el DTO a la entidad embebida pasando por {@link Scope#build}, única vía de construcción (D-09).
     * Un dto null da {@link Scope#empty()}; tooth null da lista vacía; fullMouth null o false da false (D-05).
     */
    default Scope toScope(ScopeDTO dto) {
        if (dto == null) {
            return Scope.empty();
        }
        return Scope.build(dto.tooth(), dto.toothFace(), dto.quadrant(), dto.maxillary(), Boolean.TRUE.equals(dto.fullMouth()));
    }
}
