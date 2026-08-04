package com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto;

import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.enums.PrestationStepStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record PrestationStepUpdateRequestDTO(

        // a qué PrestationInstance existente pertenece (referenciada por su id real, ya persistida)
        @NotNull(message = "prestationStepUpdateRequestDTO.prestationInstanceId.empty")
        Long prestationInstanceId,

        // null = step nuevo sobre esa prestación existente; con valor = PrestationStepInstance ya persistido
        Long id,

        // referencia al catálogo PrestationStep; obligatorio cuando id == null, irrelevante si id tiene valor
        Long prestationStepId,

        // obligatorio cuando enabled == true; irrelevante cuando enabled == false
        PrestationStepStatus status,

        // true=vigente (alta nueva o step existente sin baja); false=baja explícita del step individual
        @NotNull(message = "prestationStepUpdateRequestDTO.enabled.empty")
        Boolean enabled

) {

    // Rechaza alta+baja combinada (id==null && enabled==false) — un step que nunca existió no puede nacer dado de baja (400)
    @AssertTrue(message = "prestationStepUpdateRequestDTO.notContradictory")
    public boolean isNotContradictory() {
        return !(id == null && Boolean.FALSE.equals(enabled));
    }

    // Rechaza enabled==true sin status declarado — no existe semántica de "null = sin cambios" (400)
    @AssertTrue(message = "prestationStepUpdateRequestDTO.statusDeclaredWhenEnabled")
    public boolean isStatusDeclaredWhenEnabled() {
        return !(Boolean.TRUE.equals(enabled) && status == null);
    }

    // Rechaza alta (id==null) sin prestationStepId — el backend no sabe qué PrestationStep construir (400)
    @AssertTrue(message = "prestationStepUpdateRequestDTO.prestationStepIdDeclaredWhenNew")
    public boolean isPrestationStepIdDeclaredWhenNew() {
        return !(id == null && prestationStepId == null);
    }
}