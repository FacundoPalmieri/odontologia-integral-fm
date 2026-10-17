package com.odontologiaintegralfm.feature.consultation.treatmentinstance.dto;

import com.odontologiaintegralfm.feature.consultation.treatmentinstance.dto.TreatmentInstanceRefDTO;
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
 * Forma de la referencia a un tratamiento (existente por id o naciendo en el request por tempId).
 * Todos los casos pasan por un Validator real: así se prueba también que la anotación @AssertTrue existe.
 */
@Tag("unit")
class TreatmentInstanceRefDTOTest {

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

    private static List<String> paths(TreatmentInstanceRefDTO dto) {
        return validator.validate(dto).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .toList();
    }

    /**
     * CASO: La referencia trae id y tempId a la vez, y el caso de ninguno de los dos.
     * Regla: D-38 — exactamente uno de los dos identificadores.
     * Validación: en ambos, una única violación con propertyPath exactlyOneIdentifier (400 de forma).
     */
    @Test
    void isExactlyOneIdentifier_bothOrNone_returnsFalse() {
        assertThat(paths(new TreatmentInstanceRefDTO(1L, "tmp-1"))).containsExactly("exactlyOneIdentifier");
        assertThat(paths(new TreatmentInstanceRefDTO(null, null))).containsExactly("exactlyOneIdentifier");
    }

    /**
     * CASO: Solo id (tratamiento existente de Bety) y solo tempId (tratamiento que nace en el request).
     * Regla: D-38 — exactamente uno poblado es válido.
     * Validación: ninguna violación con propertyPath exactlyOneIdentifier en ambos.
     */
    @Test
    void isExactlyOneIdentifier_onlyIdOrOnlyTempId_returnsTrue() {
        assertThat(paths(new TreatmentInstanceRefDTO(1L, null))).doesNotContain("exactlyOneIdentifier");
        assertThat(paths(new TreatmentInstanceRefDTO(null, "tmp-1"))).doesNotContain("exactlyOneIdentifier");
    }
}
