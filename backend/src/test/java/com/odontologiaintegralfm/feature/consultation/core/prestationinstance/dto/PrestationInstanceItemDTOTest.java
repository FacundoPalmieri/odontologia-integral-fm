package com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto;

import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.enums.DiscountType;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.enums.PrestationStepStatus;
import com.odontologiaintegralfm.feature.consultation.core.scope.dto.ScopeDTO;
import com.odontologiaintegralfm.feature.consultation.core.scope.enums.Tooth;
import com.odontologiaintegralfm.feature.consultation.core.treatmentinstance.dto.TreatmentInstanceRefDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Forma de un ítem de prestation[]: con id (existente, solo avanza steps) o sin id (nace ahora).
 * Todos los casos pasan por un Validator real: así el test también falla si falta el @AssertTrue
 * (o el @NotEmpty / @Positive) en el DTO. Se afirma el propertyPath de la regla, nunca el mensaje.
 */
@Tag("unit")
class PrestationInstanceItemDTOTest {

    private static final TreatmentInstanceRefDTO REF = new TreatmentInstanceRefDTO(null, "tmp-1");
    private static final TreatmentInstanceRefDTO ORIGIN = new TreatmentInstanceRefDTO(145L, null);
    private static final ScopeDTO SCOPE = new ScopeDTO(List.of(Tooth.T24), null, null, null, null);
    private static final List<StepItemDTO> EXISTING_STEPS = List.of(new StepItemDTO(9L, null, PrestationStepStatus.FINISHED));
    private static final List<StepItemDTO> NEW_STEPS = List.of(new StepItemDTO(null, 3L, PrestationStepStatus.IN_PROGRESS));

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

    private static List<String> paths(PrestationInstanceItemDTO dto) {
        return validator.validate(dto).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .toList();
    }

    /** Prestación que nace con una ubicación válida (REF) para que la regla de ubicación no dispare ruido. */
    private static PrestationInstanceItemDTO newItem(TreatmentInstanceRefDTO ref, ScopeDTO scope, TreatmentInstanceRefDTO origin) {
        return new PrestationInstanceItemDTO(null, 3L, ref, scope, origin, null, null, null, NEW_STEPS);
    }

    private static PrestationInstanceItemDTO discountItem(Long promotionId, DiscountType type, BigDecimal value) {
        return new PrestationInstanceItemDTO(null, 3L, REF, null, null, promotionId, type, value, NEW_STEPS);
    }

    /** Con id: prestación existente (solo id + steps). Sin id: prestación nueva con ubicación válida (REF). */
    private static PrestationInstanceItemDTO stepsItem(Long id, List<StepItemDTO> steps) {
        return id == null
                ? new PrestationInstanceItemDTO(null, 3L, REF, null, null, null, null, null, steps)
                : new PrestationInstanceItemDTO(id, null, null, null, null, null, null, null, steps);
    }

    // ─── Forma de existente / nueva ───────────────────────────────────────────

    static Stream<Arguments> existingWithExtraField() {
        List<String> only = List.of("existingOnlyIdAndSteps");
        // discountType solo y discountValue solo rompen además isDiscountConsistent (tipo sin valor / valor sin tipo): dos violaciones legítimas
        List<String> withDiscount = List.of("existingOnlyIdAndSteps", "discountConsistent");
        return Stream.of(
                Arguments.of("prestationTypeId", new PrestationInstanceItemDTO(812L, 3L, null, null, null, null, null, null, EXISTING_STEPS), only),
                Arguments.of("treatmentInstanceRef", new PrestationInstanceItemDTO(812L, null, REF, null, null, null, null, null, EXISTING_STEPS), only),
                Arguments.of("scope", new PrestationInstanceItemDTO(812L, null, null, SCOPE, null, null, null, null, EXISTING_STEPS), only),
                Arguments.of("originTreatmentInstanceRef", new PrestationInstanceItemDTO(812L, null, null, null, ORIGIN, null, null, null, EXISTING_STEPS), only),
                Arguments.of("promotionId", new PrestationInstanceItemDTO(812L, null, null, null, null, 7L, null, null, EXISTING_STEPS), only),
                Arguments.of("discountType", new PrestationInstanceItemDTO(812L, null, null, null, null, null, DiscountType.FIXED, null, EXISTING_STEPS), withDiscount),
                Arguments.of("discountValue", new PrestationInstanceItemDTO(812L, null, null, null, null, null, null, BigDecimal.TEN, EXISTING_STEPS), withDiscount));
    }

    /**
     * CASO: Prestación 812 existente que además trae, uno por vez, prestationTypeId, treatmentInstanceRef, scope, originTreatmentInstanceRef,
     * promotionId, discountType o discountValue.
     * Regla: D-33 — con id solo se aceptan id y steps.
     * Validación: el Validator real devuelve la violación con propertyPath existingOnlyIdAndSteps en cada caso. En discountType solo y
     * discountValue solo aparece además discountConsistent (un tipo sin valor o un valor sin tipo también es inconsistente por D-30);
     * por eso esos dos casos esperan las dos violaciones y los otros cinco una sola.
     */
    @ParameterizedTest(name = "id + {0}")
    @MethodSource("existingWithExtraField")
    void isExistingOnlyIdAndSteps_idWithAnyOtherField_returnsFalse(String field, PrestationInstanceItemDTO dto, List<String> expectedPaths) {
        assertThat(paths(dto)).containsExactlyInAnyOrderElementsOf(expectedPaths);
    }

    /**
     * CASO: { id: 812, steps: [...] }.
     * Regla: D-33 — id + steps es la forma válida de una prestación existente.
     * Validación: ninguna violación tiene propertyPath existingOnlyIdAndSteps.
     */
    @Test
    void isExistingOnlyIdAndSteps_idWithSteps_returnsTrue() {
        assertThat(paths(stepsItem(812L, EXISTING_STEPS))).doesNotContain("existingOnlyIdAndSteps");
    }

    /**
     * CASO: Prestación sin id y sin prestationTypeId.
     * Regla: si nace, el tipo de prestación es obligatorio.
     * Validación: una única violación, con propertyPath newHasPrestationType.
     */
    @Test
    void isNewHasPrestationType_missingPrestationTypeId_returnsFalse() {
        PrestationInstanceItemDTO dto = new PrestationInstanceItemDTO(null, null, REF, null, null, null, null, null, NEW_STEPS);

        assertThat(paths(dto)).containsExactly("newHasPrestationType");
    }

    // ─── Ubicación (D-24) ─────────────────────────────────────────────────────

    static Stream<Arguments> validLocations() {
        return Stream.of(
                Arguments.of("solo treatmentInstanceRef", newItem(REF, null, null)),
                Arguments.of("solo scope", newItem(null, SCOPE, null)),
                Arguments.of("treatmentInstanceRef + origin (Agregado de Alan)", newItem(REF, null, ORIGIN)),
                Arguments.of("solo origin", newItem(null, null, ORIGIN)));
    }

    static Stream<Arguments> invalidLocations() {
        return Stream.of(
                Arguments.of("treatmentInstanceRef + scope", newItem(REF, SCOPE, null)),
                Arguments.of("scope + origin", newItem(null, SCOPE, ORIGIN)),
                Arguments.of("las tres", newItem(REF, SCOPE, ORIGIN)),
                Arguments.of("ninguna", newItem(null, null, null)));
    }

    /**
     * CASO: Las cuatro combinaciones válidas de una prestación que nace.
     * Regla: D-24 ajustado — treatmentInstanceRef; scope; treatmentInstanceRef + origin; origin sola.
     * Validación: ninguna violación tiene propertyPath locationConsistent en los cuatro.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("validLocations")
    void isLocationConsistent_validCombinations_returnsTrue(String label, PrestationInstanceItemDTO dto) {
        assertThat(paths(dto)).doesNotContain("locationConsistent");
    }

    /**
     * CASO: Las combinaciones inválidas: referencia + scope; scope + origin; las tres; ninguna.
     * Regla: D-24 ajustado — el XOR de forma es entre treatmentInstanceRef y scope; scope + origin y ninguna son 400.
     * Validación: una única violación, con propertyPath locationConsistent, en los cuatro.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidLocations")
    void isLocationConsistent_invalidCombinations_returnsFalse(String label, PrestationInstanceItemDTO dto) {
        assertThat(paths(dto)).containsExactly("locationConsistent");
    }

    // ─── Descuento (D-30) ─────────────────────────────────────────────────────

    /**
     * CASO: promotionId y discountValue juntos.
     * Regla: D-30 — promoción y descuento manual son excluyentes.
     * Validación: una única violación, con propertyPath discountConsistent.
     */
    @Test
    void isDiscountConsistent_promotionWithDiscountValue_returnsFalse() {
        assertThat(paths(discountItem(7L, DiscountType.FIXED, BigDecimal.TEN))).containsExactly("discountConsistent");
    }

    /**
     * CASO: discountType sin discountValue, y discountValue sin discountType.
     * Regla: D-30 — tipo y valor van juntos o ninguno.
     * Validación: una única violación, con propertyPath discountConsistent, en ambos.
     */
    @Test
    void isDiscountConsistent_discountTypeWithoutValueOrViceversa_returnsFalse() {
        assertThat(paths(discountItem(null, DiscountType.PERCENTAGE, null))).containsExactly("discountConsistent");
        assertThat(paths(discountItem(null, null, BigDecimal.TEN))).containsExactly("discountConsistent");
    }

    /**
     * CASO: sin ningún campo de descuento; discountType + discountValue; solo promotionId.
     * Regla: D-30 — son las combinaciones válidas.
     * Validación: ninguna violación tiene propertyPath discountConsistent en las tres.
     */
    @Test
    void isDiscountConsistent_noneOrBothDiscountFields_returnsTrue() {
        assertThat(paths(discountItem(null, null, null))).doesNotContain("discountConsistent");
        assertThat(paths(discountItem(null, DiscountType.PERCENTAGE, BigDecimal.TEN))).doesNotContain("discountConsistent");
        assertThat(paths(discountItem(7L, null, null))).doesNotContain("discountConsistent");
    }

    /**
     * CASO: Prestación nueva válida de Bety con steps = [].
     * Regla: D-32 — toda prestación trae su lista de steps, nunca vacía.
     * Validación: una única violación, con propertyPath steps.
     */
    @Test
    void steps_emptyList_violatesNotEmpty() {
        PrestationInstanceItemDTO dto = new PrestationInstanceItemDTO(null, 3L, REF, null, null, null, null, null, List.of());

        assertThat(paths(dto)).containsExactly("steps");
    }

    /**
     * CASO: discountValue = 0 y -5 (con discountType para no violar otra regla), y 10 como control.
     * Regla: el valor de descuento manual es positivo.
     * Validación: una única violación, con propertyPath discountValue, en 0 y -5; ninguna con 10.
     */
    @ParameterizedTest
    @ValueSource(ints = {0, -5})
    void discountValue_nonPositive_violatesPositive(int value) {
        assertThat(paths(discountItem(null, DiscountType.FIXED, BigDecimal.valueOf(value)))).containsExactly("discountValue");
        assertThat(paths(discountItem(null, DiscountType.FIXED, BigDecimal.TEN))).isEmpty();
    }

    // ─── Steps ────────────────────────────────────────────────────────────────

    /**
     * CASO: Prestación sin id con dos steps {stepId: 3} y {stepId: 4}; y una prestación con id.
     * Regla: D-22 — en una prestación que nace todos los steps traen stepId; con id la regla no aplica.
     * Validación: ninguna violación tiene propertyPath newStepsDeclareCatalogStep en ambos.
     */
    @Test
    void isNewStepsDeclareCatalogStep_newPrestationWithStepIdOnly_returnsTrue() {
        List<StepItemDTO> twoCatalogSteps = List.of(
                new StepItemDTO(null, 3L, PrestationStepStatus.IN_PROGRESS),
                new StepItemDTO(null, 4L, PrestationStepStatus.IN_PROGRESS));

        assertThat(paths(stepsItem(null, twoCatalogSteps))).doesNotContain("newStepsDeclareCatalogStep");
        assertThat(paths(stepsItem(812L, EXISTING_STEPS))).doesNotContain("newStepsDeclareCatalogStep");
    }

    /**
     * CASO: Prestación sin id con un step {id: 9}, y con un step sin id ni stepId.
     * Regla: D-22 — una prestación que nace no puede tener steps existentes ni steps sin identificar.
     * Validación: newStepsDeclareCatalogStep es la única violación con el step {id: 9}. Con el step sin id ni stepId
     * aparece además la del propio StepItemDTO anidado (steps[0].idXorStepId), que es la regla de D-33 y no ruido.
     */
    @Test
    void isNewStepsDeclareCatalogStep_newPrestationStepWithIdOrWithoutStepId_returnsFalse() {
        List<StepItemDTO> existingStep = List.of(new StepItemDTO(9L, null, PrestationStepStatus.FINISHED));
        List<StepItemDTO> stepWithoutIdentifier = List.of(new StepItemDTO(null, null, PrestationStepStatus.FINISHED));

        assertThat(paths(stepsItem(null, existingStep))).containsExactly("newStepsDeclareCatalogStep");
        assertThat(paths(stepsItem(null, stepWithoutIdentifier)))
                .containsExactlyInAnyOrder("newStepsDeclareCatalogStep", "steps[0].idXorStepId");
    }

    /**
     * CASO: Una prestación con dos steps {stepId: 3}; y otra con dos steps {id: 9}.
     * Regla: D-23 — dentro de una prestación no se repite un stepId ni un id de step.
     * Validación: una única violación, con propertyPath stepsUnique, en ambos.
     */
    @Test
    void isStepsUnique_duplicatedStepIdOrId_returnsFalse() {
        List<StepItemDTO> duplicatedStepId = List.of(
                new StepItemDTO(null, 3L, PrestationStepStatus.FINISHED),
                new StepItemDTO(null, 3L, PrestationStepStatus.IN_PROGRESS));
        List<StepItemDTO> duplicatedId = List.of(
                new StepItemDTO(9L, null, PrestationStepStatus.FINISHED),
                new StepItemDTO(9L, null, PrestationStepStatus.CANCELED));

        assertThat(paths(stepsItem(null, duplicatedStepId))).containsExactly("stepsUnique");
        assertThat(paths(stepsItem(812L, duplicatedId))).containsExactly("stepsUnique");
    }

    /**
     * CASO: steps {stepId: 3} y {stepId: 4}; y {id: 9} y {id: 10}.
     * Regla: D-23 — steps distintos son válidos.
     * Validación: ninguna violación tiene propertyPath stepsUnique en ambos.
     */
    @Test
    void isStepsUnique_distinctSteps_returnsTrue() {
        List<StepItemDTO> distinctStepIds = List.of(
                new StepItemDTO(null, 3L, PrestationStepStatus.FINISHED),
                new StepItemDTO(null, 4L, PrestationStepStatus.IN_PROGRESS));
        List<StepItemDTO> distinctIds = List.of(
                new StepItemDTO(9L, null, PrestationStepStatus.FINISHED),
                new StepItemDTO(10L, null, PrestationStepStatus.FINISHED));

        assertThat(paths(stepsItem(null, distinctStepIds))).doesNotContain("stepsUnique");
        assertThat(paths(stepsItem(812L, distinctIds))).doesNotContain("stepsUnique");
    }

    /**
     * CASO: Prestación existente 812 con un step existente {id: 3} y un step de catálogo {stepId: 3} (tablas distintas).
     * Regla: D-23 — id de step y stepId de catálogo son identificadores de tablas distintas; el mismo número no es duplicado.
     * Validación: ninguna violación tiene propertyPath stepsUnique. Falla si la regla usa un solo Set para ambos identificadores.
     */
    @Test
    void isStepsUnique_sameIdAndStepIdInSamePrestation_returnsTrue() {
        List<StepItemDTO> sameNumberDifferentTables = List.of(
                new StepItemDTO(3L, null, PrestationStepStatus.FINISHED),
                new StepItemDTO(null, 3L, PrestationStepStatus.IN_PROGRESS));

        assertThat(paths(stepsItem(812L, sameNumberDifferentTables))).doesNotContain("stepsUnique");
    }

    /**
     * CASO: Prestación nueva (tipo 3, ubicación vía referencia) con steps = null, pasada por el Validator real.
     * Regla: isStepsUnique e isNewStepsDeclareCatalogStep son null-safe; el 400 por steps ausente lo da @NotEmpty.
     * Validación: no lanza ValidationException y ninguna violación tiene propertyPath stepsUnique ni newStepsDeclareCatalogStep.
     */
    @Test
    void steps_null_doesNotThrowAndSkipsAssertTrueRules() {
        PrestationInstanceItemDTO dto = stepsItem(null, null);

        assertThatCode(() -> paths(dto)).doesNotThrowAnyException();
        assertThat(paths(dto)).doesNotContain("stepsUnique", "newStepsDeclareCatalogStep");
    }
}
