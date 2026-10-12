package com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto;

import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.enums.PrestationStepStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Forma de un step del request: id (step existente) xor stepId (step que nace) y status obligatorio.
 * Todos los casos pasan por un Validator real: así se prueba también que la anotación existe.
 */
@Tag("unit")
class StepItemDTOTest {

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

    private static List<String> paths(StepItemDTO dto) {
        return validator.validate(dto).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .toList();
    }

    /**
     * CASO: Step con id y stepId a la vez, y step sin ninguno de los dos (status válido en ambos).
     * Regla: D-33 — exactamente uno de id/stepId.
     * Validación: en ambos, una única violación con propertyPath idXorStepId (400 de forma).
     */
    @Test
    void isIdXorStepId_bothOrNone_returnsFalse() {
        assertThat(paths(new StepItemDTO(9L, 3L, PrestationStepStatus.FINISHED))).containsExactly("idXorStepId");
        assertThat(paths(new StepItemDTO(null, null, PrestationStepStatus.FINISHED))).containsExactly("idXorStepId");
    }

    /**
     * CASO: Step existente solo con id y step nuevo solo con stepId.
     * Regla: D-33 — el id solo viaja con un step existente y el stepId solo cuando el step nace.
     * Validación: ninguna violación con propertyPath idXorStepId en ambos.
     */
    @Test
    void isIdXorStepId_onlyIdOrOnlyStepId_returnsTrue() {
        assertThat(paths(new StepItemDTO(9L, null, PrestationStepStatus.FINISHED))).doesNotContain("idXorStepId");
        assertThat(paths(new StepItemDTO(null, 3L, PrestationStepStatus.IN_PROGRESS))).doesNotContain("idXorStepId");
    }

    /**
     * CASO: Ítem base válido: step nuevo con stepId 3, sin id y status IN_PROGRESS.
     * Regla: referencia contra la que se arman los casos de rechazo de esta suite.
     * Validación: cero violaciones.
     */
    @Test
    void baseItem_valid_hasNoViolations() {
        assertThat(paths(new StepItemDTO(null, 3L, PrestationStepStatus.IN_PROGRESS))).isEmpty();
    }

    /**
     * CASO: Step nuevo (stepId 3) con status null.
     * Regla: el status es obligatorio en todo step.
     * Validación: una única violación con propertyPath status.
     */
    @Test
    void status_null_violatesNotNull() {
        assertThat(paths(new StepItemDTO(null, 3L, null))).containsExactly("status");
    }
}
