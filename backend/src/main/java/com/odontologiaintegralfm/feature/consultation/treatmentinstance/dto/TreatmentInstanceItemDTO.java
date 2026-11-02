package com.odontologiaintegralfm.feature.consultation.treatmentinstance.dto;

import com.odontologiaintegralfm.feature.consultation.treatment.enums.TreatmentConditionType;
import com.odontologiaintegralfm.feature.consultation.scope.dto.ScopeDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;

/**
 * Ítem de treatmentInstance[]: con id cierra un tratamiento existente en REQUIRED (REPLACED/CANCELED);
 * sin id nace uno nuevo (D-22, D-33, D-51).
 */
public record TreatmentInstanceItemDTO(
        Long id,
        String tempId,
        @Valid ScopeDTO scope,
        Long treatmentId,
        TreatmentConditionType treatmentCondition
) {

    @AssertTrue(message = "treatmentInstanceItemDTO.existingOnlyCondition")
    public boolean isExistingOnlyCondition() {
        return id == null || (tempId == null && !hasLocation() && treatmentId == null);
    }

    @AssertTrue(message = "treatmentInstanceItemDTO.existingConditionReplacedOrCanceled")
    public boolean isExistingConditionReplacedOrCanceled() {
        return id == null
                || treatmentCondition == TreatmentConditionType.REPLACED
                || treatmentCondition == TreatmentConditionType.CANCELED;
    }

    @AssertTrue(message = "treatmentInstanceItemDTO.newHasScope")
    public boolean isNewHasScope() {
        return id != null || hasLocation();
    }

    @AssertTrue(message = "treatmentInstanceItemDTO.newHasTreatment")
    public boolean isNewHasTreatment() {
        return id != null || treatmentId != null;
    }

    @AssertTrue(message = "treatmentInstanceItemDTO.newConditionAllowed")
    public boolean isNewConditionAllowed() {
        return id != null
                || treatmentCondition == null
                || treatmentCondition == TreatmentConditionType.REQUIRED
                || treatmentCondition == TreatmentConditionType.PREEXISTING;
    }

    private boolean hasLocation() {
        return scope != null && scope.hasAnyLocation();
    }
}
