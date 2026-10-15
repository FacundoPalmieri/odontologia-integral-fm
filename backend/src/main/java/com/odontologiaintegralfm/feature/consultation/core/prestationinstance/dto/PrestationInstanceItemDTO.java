package com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto;

import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.enums.DiscountType;
import com.odontologiaintegralfm.feature.consultation.core.scope.dto.ScopeDTO;
import com.odontologiaintegralfm.feature.consultation.core.treatmentinstance.dto.TreatmentInstanceRefDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Ítem de prestation[]: con id avanza los steps de una prestación existente (solo id + steps);
 * sin id nace una nueva (D-22, D-24, D-30, D-32, D-33).
 */
public record PrestationInstanceItemDTO(
        Long id,
        Long prestationTypeId,
        @Valid TreatmentInstanceRefDTO treatmentInstanceRef,
        @Valid ScopeDTO scope,
        @Valid TreatmentInstanceRefDTO originTreatmentInstanceRef,
        Long promotionId,
        DiscountType discountType,
        @Positive(message = "prestationInstanceItemDTO.discountValue.positive")
        BigDecimal discountValue,
        @NotEmpty(message = "prestationInstanceItemDTO.steps.empty")
        List<@NotNull(message = "prestationInstanceItemDTO.steps.nullItem") @Valid StepItemDTO> steps
) {

    @AssertTrue(message = "prestationInstanceItemDTO.existingOnlyIdAndSteps")
    public boolean isExistingOnlyIdAndSteps() {
        return id == null
                || (prestationTypeId == null && treatmentInstanceRef == null && !hasOwnScope()
                && originTreatmentInstanceRef == null && promotionId == null
                && discountType == null && discountValue == null);
    }

    @AssertTrue(message = "prestationInstanceItemDTO.newHasPrestationType")
    public boolean isNewHasPrestationType() {
        return id != null || prestationTypeId != null;
    }

    @AssertTrue(message = "prestationInstanceItemDTO.newStepsDeclareCatalogStep")
    public boolean isNewStepsDeclareCatalogStep() {
        if (id != null || steps == null) return true;
        return steps.stream().allMatch(step -> step != null && step.stepId() != null && step.id() == null);
    }

    @AssertTrue(message = "prestationInstanceItemDTO.stepsUnique")
    public boolean isStepsUnique() {
        if (steps == null) return true;
        Set<Long> stepIds = new HashSet<>();
        Set<Long> ids = new HashSet<>();
        for (StepItemDTO step : steps) {
            if (step == null) continue;
            if (step.stepId() != null && !stepIds.add(step.stepId())) return false;
            if (step.id() != null && !ids.add(step.id())) return false;
        }
        return true;
    }

    /**
     * Combinaciones válidas al nacer: referencia sola, scope solo, referencia + origen, origen solo.
     * El XOR de forma es entre referencia y scope; scope + origen y ninguna de las tres son inválidas (D-24).
     */
    @AssertTrue(message = "prestationInstanceItemDTO.locationConsistent")
    public boolean isLocationConsistent() {
        if (id != null) return true;
        boolean hasRef = treatmentInstanceRef != null;
        boolean hasScope = hasOwnScope();
        boolean hasOrigin = originTreatmentInstanceRef != null;
        if (hasRef && hasScope) return false;
        if (hasScope && hasOrigin) return false;
        return hasRef || hasScope || hasOrigin;
    }

    @AssertTrue(message = "prestationInstanceItemDTO.discountConsistent")
    public boolean isDiscountConsistent() {
        if (promotionId != null && discountValue != null) return false;
        return (discountType != null) == (discountValue != null);
    }

    private boolean hasOwnScope() {
        return scope != null && scope.hasAnyLocation();
    }
}
