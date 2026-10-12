package com.odontologiaintegralfm.feature.consultation.core.treatmentinstance.dto;

import com.odontologiaintegralfm.feature.consultation.catalogs.treatment.enums.TreatmentConditionType;
import com.odontologiaintegralfm.feature.consultation.core.scope.dto.ScopeDTO;
import com.odontologiaintegralfm.feature.consultation.core.scope.enums.Quadrant;
import com.odontologiaintegralfm.feature.consultation.core.scope.enums.Tooth;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Forma de un ítem de treatmentInstance[]: con id (existente en REQUIRED que se cierra) o sin id (nace ahora).
 * Todos los casos pasan por un Validator real: así el test también falla si falta el @AssertTrue en el DTO.
 * Se afirma el propertyPath de la regla, nunca el mensaje.
 */
@Tag("unit")
class TreatmentInstanceItemDTOTest {

    private static final ScopeDTO TOOTH_16 = new ScopeDTO(List.of(Tooth.T16), null, null, null, null);

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    private static List<String> paths(TreatmentInstanceItemDTO dto) {
        return validator.validate(dto).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .toList();
    }

    /**
     * CASO: Tratamiento 145 existente que además trae scope, o treatmentId, o tempId.
     * Regla: D-33 — con id no se acepta ningún otro campo salvo la condición.
     * Validación: una única violación, con propertyPath existingOnlyCondition, en cada uno.
     */
    @Test
    void isExistingOnlyCondition_idWithScopeOrTreatmentId_returnsFalse() {
        assertThat(paths(new TreatmentInstanceItemDTO(145L, null, TOOTH_16, null, TreatmentConditionType.REPLACED)))
                .containsExactly("existingOnlyCondition");
        assertThat(paths(new TreatmentInstanceItemDTO(145L, null, null, 5L, TreatmentConditionType.REPLACED)))
                .containsExactly("existingOnlyCondition");
        assertThat(paths(new TreatmentInstanceItemDTO(145L, "tmp-1", null, null, TreatmentConditionType.REPLACED)))
                .containsExactly("existingOnlyCondition");
    }

    /**
     * CASO: { id: 145, treatmentCondition: REPLACED }.
     * Regla: D-33 — el único contenido permitido junto al id es la condición.
     * Validación: ninguna violación tiene propertyPath existingOnlyCondition.
     */
    @Test
    void isExistingOnlyCondition_idWithOnlyCondition_returnsTrue() {
        assertThat(paths(new TreatmentInstanceItemDTO(145L, null, null, null, TreatmentConditionType.REPLACED)))
                .doesNotContain("existingOnlyCondition");
    }

    /**
     * CASO: Tratamiento existente con condición REPLACED y con CANCELED.
     * Regla: D-33 — con id la condición solo puede ser REPLACED o CANCELED.
     * Validación: ninguna violación tiene propertyPath existingConditionReplacedOrCanceled en ambos.
     */
    @Test
    void isExistingConditionReplacedOrCanceled_validValues_returnsTrue() {
        assertThat(paths(new TreatmentInstanceItemDTO(145L, null, null, null, TreatmentConditionType.REPLACED)))
                .doesNotContain("existingConditionReplacedOrCanceled");
        assertThat(paths(new TreatmentInstanceItemDTO(145L, null, null, null, TreatmentConditionType.CANCELED)))
                .doesNotContain("existingConditionReplacedOrCanceled");
    }

    /**
     * CASO: Tratamiento existente con condición null, REQUIRED, IN_PROGRESS, FINISHED y PREEXISTING.
     * Regla: D-33 — cualquier otro valor (o ausencia) con id es 400.
     * Validación: una única violación, con propertyPath existingConditionReplacedOrCanceled, en los cinco.
     */
    @ParameterizedTest
    @NullSource
    @EnumSource(value = TreatmentConditionType.class, names = {"REQUIRED", "IN_PROGRESS", "FINISHED", "PREEXISTING"})
    void isExistingConditionReplacedOrCanceled_missingOrOtherValue_returnsFalse(TreatmentConditionType condition) {
        assertThat(paths(new TreatmentInstanceItemDTO(145L, null, null, null, condition)))
                .containsExactly("existingConditionReplacedOrCanceled");
    }

    /**
     * CASO: Tratamiento nuevo (sin id) con scope null y con scope vacío.
     * Regla: D-10 — si nace, el scope trae exactamente un grupo poblado.
     * Validación: una única violación, con propertyPath newHasScope, en ambos.
     */
    @Test
    void isNewHasScope_emptyScopeOrNull_returnsFalse() {
        assertThat(paths(new TreatmentInstanceItemDTO(null, null, null, 5L, null))).containsExactly("newHasScope");
        assertThat(paths(new TreatmentInstanceItemDTO(null, null, new ScopeDTO(null, null, null, null, null), 5L, null)))
                .containsExactly("newHasScope");
    }

    /**
     * CASO: Tratamiento nuevo con scope que trae solo quadrant.
     * Regla: D-10 — un grupo poblado es válido.
     * Validación: ninguna violación tiene propertyPath newHasScope.
     */
    @Test
    void isNewHasScope_oneGroupPopulated_returnsTrue() {
        ScopeDTO quadrantOnly = new ScopeDTO(null, null, Quadrant.UPPER_RIGHT, null, null);

        assertThat(paths(new TreatmentInstanceItemDTO(null, null, quadrantOnly, 5L, null))).doesNotContain("newHasScope");
    }

    /**
     * CASO: Tratamiento nuevo (treatmentId 5) con scope.tooth = [] y ningún otro grupo.
     * Regla: D-10 — una lista de dientes vacía no cuenta como ubicación; el tratamiento que nace necesita scope poblado.
     * Validación: una única violación, con propertyPath newHasScope.
     */
    @Test
    void isNewHasScope_emptyToothListOnly_returnsFalse() {
        assertThat(paths(new TreatmentInstanceItemDTO(null, null, new ScopeDTO(List.of(), null, null, null, null), 5L, null)))
                .containsExactly("newHasScope");
    }

    /**
     * CASO: Ítem base válido: tratamiento nuevo con diente 16, treatmentId 5, sin tempId y sin condición.
     * Regla: referencia contra la que se arman los casos de rechazo de esta suite.
     * Validación: cero violaciones.
     */
    @Test
    void baseItem_valid_hasNoViolations() {
        assertThat(paths(new TreatmentInstanceItemDTO(null, null, TOOTH_16, 5L, null))).isEmpty();
    }

    /**
     * CASO: Tratamiento nuevo sin id y sin treatmentId.
     * Regla: si nace, el treatmentId del catálogo es obligatorio.
     * Validación: una única violación, con propertyPath newHasTreatment.
     */
    @Test
    void isNewHasTreatment_missingTreatmentId_returnsFalse() {
        assertThat(paths(new TreatmentInstanceItemDTO(null, null, TOOTH_16, null, null))).containsExactly("newHasTreatment");
    }

    /**
     * CASO: Tratamiento nuevo con condición null, REQUIRED y PREEXISTING.
     * Regla: D-51 / decisión 10 — al nacer solo se admite condición ausente, REQUIRED o PREEXISTING.
     * Validación: ninguna violación tiene propertyPath newConditionAllowed en los tres.
     */
    @ParameterizedTest
    @NullSource
    @EnumSource(value = TreatmentConditionType.class, names = {"REQUIRED", "PREEXISTING"})
    void isNewConditionAllowed_nullRequiredOrPreexisting_returnsTrue(TreatmentConditionType condition) {
        assertThat(paths(new TreatmentInstanceItemDTO(null, null, TOOTH_16, 5L, condition)))
                .doesNotContain("newConditionAllowed");
    }

    /**
     * CASO: Tratamiento nuevo con condición IN_PROGRESS, FINISHED, REPLACED y CANCELED.
     * Regla: D-51 — esos estados solo se alcanzan por derivación o por cierre explícito de un existente.
     * Validación: una única violación, con propertyPath newConditionAllowed, en los cuatro.
     */
    @ParameterizedTest
    @EnumSource(value = TreatmentConditionType.class, names = {"IN_PROGRESS", "FINISHED", "REPLACED", "CANCELED"})
    void isNewConditionAllowed_inProgressFinishedReplacedCanceled_returnsFalse(TreatmentConditionType condition) {
        assertThat(paths(new TreatmentInstanceItemDTO(null, null, TOOTH_16, 5L, condition)))
                .containsExactly("newConditionAllowed");
    }
}
