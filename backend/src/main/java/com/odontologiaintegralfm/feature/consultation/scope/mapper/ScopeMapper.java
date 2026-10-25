package com.odontologiaintegralfm.feature.consultation.scope.mapper;

import com.odontologiaintegralfm.feature.consultation.scope.dto.ScopeDTO;
import com.odontologiaintegralfm.feature.consultation.scope.model.Scope;
import com.odontologiaintegralfm.feature.consultation.scope.enums.Tooth;
import org.mapstruct.Mapper;

import java.util.List;

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

    /**
     * Convierte la entidad embebida al DTO de respuesta (D-06). Los grupos sin usar van en null:
     * tooth null si la lista está vacía y fullMouth null si es false.
     */
    default ScopeDTO toScopeDTO(Scope scope) {
        List<Tooth> tooth = scope.getTooth() == null || scope.getTooth().isEmpty() ? null : scope.getTooth();
        Boolean fullMouth = scope.isFullMouth() ? Boolean.TRUE : null;
        return new ScopeDTO(tooth, scope.getToothFace(), scope.getQuadrant(), scope.getMaxillary(), fullMouth);
    }
}
