package com.odontologiaintegralfm.feature.consultation.consultationinstance.dto;

import com.odontologiaintegralfm.feature.consultation.prestationinstance.dto.PrestationInstanceItemDTO;
import com.odontologiaintegralfm.feature.consultation.treatmentinstance.dto.TreatmentInstanceItemDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Request del create: tratamientos y prestaciones de la consulta. id presente = existente; ausente = nace ahora (D-21, D-22).
 * null en cualquiera de las dos listas se trata como vacía.
 */
public record ConsultationInstanceCreateRequestDTO(

        @NotNull(message = "consultationInstanceCreateRequestDTO.id.empty")
        Long consultationId,

        String observation,

        List<@NotNull(message = "consultationInstanceCreateRequestDTO.treatmentInstance.nullItem") @Valid TreatmentInstanceItemDTO> treatmentInstance,

        List<@NotNull(message = "consultationInstanceCreateRequestDTO.prestation.nullItem") @Valid PrestationInstanceItemDTO> prestation
) {

    @AssertTrue(message = "consultationInstanceCreateRequestDTO.atLeastOneClinicalData")
    public boolean isAtLeastOneClinicalDataPresent() {
        return !(isEmpty(treatmentInstance) && isEmpty(prestation));
    }

    /** Los tempId no nulos de los tratamientos no se repiten (D-23). */
    @AssertTrue(message = "consultationInstanceCreateRequestDTO.tempIdsUnique")
    public boolean isTempIdsUnique() {
        if (treatmentInstance == null) return true;
        Set<String> tempIds = new HashSet<>();
        for (TreatmentInstanceItemDTO item : treatmentInstance) {
            if (item != null && item.tempId() != null && !tempIds.add(item.tempId())) return false;
        }
        return true;
    }

    /** Los id no nulos no se repiten dentro de cada lista (D-23). */
    @AssertTrue(message = "consultationInstanceCreateRequestDTO.itemIdsUnique")
    public boolean isItemIdsUnique() {
        return noRepeatedIds(treatmentInstance == null ? List.of() : treatmentInstance.stream().filter(Objects::nonNull).map(TreatmentInstanceItemDTO::id).toList())
                && noRepeatedIds(prestation == null ? List.of() : prestation.stream().filter(Objects::nonNull).map(PrestationInstanceItemDTO::id).toList());
    }

    private static boolean isEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }

    private static boolean noRepeatedIds(List<Long> ids) {
        Set<Long> seen = new HashSet<>();
        for (Long id : ids) {
            if (id != null && !seen.add(id)) return false;
        }
        return true;
    }
}
