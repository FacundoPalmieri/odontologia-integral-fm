package com.odontologiaintegralfm.feature.consultation.scope.dto;

import com.odontologiaintegralfm.feature.consultation.scope.dto.ScopeDTO;
import com.odontologiaintegralfm.feature.consultation.scope.enums.Maxillary;
import com.odontologiaintegralfm.feature.consultation.scope.enums.Quadrant;
import com.odontologiaintegralfm.feature.consultation.scope.enums.Tooth;
import com.odontologiaintegralfm.feature.consultation.scope.enums.ToothFace;
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

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Forma de la ubicación (Scope) que viaja en el request: ADR-0030, @AssertTrue en el DTO.
 * Los casos de validación pasan por un Validator real: así se prueba también que la anotación existe.
 */
@Tag("unit")
class ScopeDTOTest {

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

    private static List<String> paths(ScopeDTO dto) {
        return validator.validate(dto).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .toList();
    }

    /**
     * CASO: toothFace TOP sin ningún diente (tooth null y tooth lista vacía).
     * Regla: D-09 — la cara exige al menos un diente.
     * Validación: en ambos, una única violación con propertyPath toothFaceWithTooth (400 de forma).
     */
    @Test
    void isToothFaceWithTooth_faceWithoutTooth_returnsFalse() {
        assertThat(paths(new ScopeDTO(null, ToothFace.TOP, null, null, null))).containsExactly("toothFaceWithTooth");
        assertThat(paths(new ScopeDTO(List.of(), ToothFace.TOP, null, null, null))).containsExactly("toothFaceWithTooth");
    }

    /**
     * CASO: diente 16 con cara TOP y diente 16 sin cara.
     * Regla: D-09 — la cara es opcional cuando hay diente.
     * Validación: ninguna violación con propertyPath toothFaceWithTooth en ambos.
     */
    @Test
    void isToothFaceWithTooth_faceWithToothOrToothWithoutFace_returnsTrue() {
        assertThat(paths(new ScopeDTO(List.of(Tooth.T16), ToothFace.TOP, null, null, null))).doesNotContain("toothFaceWithTooth");
        assertThat(paths(new ScopeDTO(List.of(Tooth.T16), null, null, null, null))).doesNotContain("toothFaceWithTooth");
    }

    static Stream<Arguments> twoGroups() {
        return Stream.of(
                Arguments.of("tooth + quadrant", new ScopeDTO(List.of(Tooth.T16), null, Quadrant.UPPER_RIGHT, null, null)),
                Arguments.of("quadrant + maxillary", new ScopeDTO(null, null, Quadrant.UPPER_RIGHT, Maxillary.UPPER, null)),
                Arguments.of("maxillary + fullMouth", new ScopeDTO(null, null, null, Maxillary.UPPER, true)));
    }

    /**
     * CASO: dos grupos de ubicación poblados a la vez (diente + cuadrante; cuadrante + maxilar; maxilar + boca completa).
     * Regla: D-10 — a lo sumo un grupo de ubicación.
     * Validación: en los tres, una única violación con propertyPath singleLocationGroup (400 de forma).
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("twoGroups")
    void isSingleLocationGroup_twoGroupsPopulated_returnsFalse(String label, ScopeDTO scope) {
        assertThat(paths(scope)).containsExactly("singleLocationGroup");
    }

    /**
     * CASO: tooth = [] (lista vacía) junto con un cuadrante poblado.
     * Regla: D-10 — una lista de dientes vacía no cuenta como grupo poblado, así que solo hay un grupo.
     * Validación: ninguna violación con propertyPath singleLocationGroup.
     */
    @Test
    void isSingleLocationGroup_emptyToothListWithOtherGroup_returnsTrue() {
        assertThat(paths(new ScopeDTO(List.of(), null, Quadrant.UPPER_RIGHT, null, null))).doesNotContain("singleLocationGroup");
    }

    /**
     * CASO: cuadrante poblado y fullMouth = false.
     * Regla: D-05 — fullMouth = false equivale a null, no cuenta como grupo poblado.
     * Validación: ninguna violación con propertyPath singleLocationGroup.
     */
    @Test
    void isSingleLocationGroup_fullMouthFalseWithAnotherGroup_returnsTrue() {
        assertThat(paths(new ScopeDTO(null, null, Quadrant.UPPER_RIGHT, null, false))).doesNotContain("singleLocationGroup");
    }

    /**
     * CASO: un solo grupo (diente con cara, cuadrante, maxilar, boca completa) y scope completamente vacío.
     * Regla: D-10 — diente + cara cuenta como un único grupo; vacío no viola el XOR.
     * Validación: ninguna violación con propertyPath singleLocationGroup en ninguno.
     */
    @Test
    void isSingleLocationGroup_oneGroupOrNone_returnsTrue() {
        assertThat(paths(new ScopeDTO(List.of(Tooth.T16), ToothFace.TOP, null, null, null))).doesNotContain("singleLocationGroup");
        assertThat(paths(new ScopeDTO(null, null, Quadrant.UPPER_RIGHT, null, null))).doesNotContain("singleLocationGroup");
        assertThat(paths(new ScopeDTO(null, null, null, Maxillary.UPPER, null))).doesNotContain("singleLocationGroup");
        assertThat(paths(new ScopeDTO(null, null, null, null, true))).doesNotContain("singleLocationGroup");
        assertThat(paths(new ScopeDTO(null, null, null, null, null))).doesNotContain("singleLocationGroup");
    }

    /**
     * CASO: scope vacío, con fullMouth = false y con fullMouth = true.
     * Regla: D-05 — solo fullMouth = true cuenta como ubicación.
     * Validación: hasAnyLocation devuelve false, false y true. Es un helper, no una regla @AssertTrue: no hay
     * anotación que verificar con Validator, por eso es el único caso de la suite que llama el método directo.
     */
    @Test
    void hasAnyLocation_emptyAndPopulatedScope_returnsExpected() {
        assertThat(new ScopeDTO(null, null, null, null, null).hasAnyLocation()).isFalse();
        assertThat(new ScopeDTO(null, null, null, null, false).hasAnyLocation()).isFalse();
        assertThat(new ScopeDTO(null, null, null, null, true).hasAnyLocation()).isTrue();
    }
}
