//package com.odontologiaintegralfm.feature.consultation.core.consultationinstance.dto;
//
//import com.odontologiaintegralfm.feature.consultation.core.odontogram.dto.OdontogramUpdateRequestDTO;
//import com.odontologiaintegralfm.feature.consultation.core.odontogram.enums.Tooth;
//import com.odontologiaintegralfm.feature.consultation.core.odontogram.enums.ToothFace;
//import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto.PrestationInstanceUpdateRequestDTO;
//import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto.PrestationStepUpdateRequestDTO;
//import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.enums.PrestationStepStatus;
//import jakarta.validation.ConstraintViolation;
//import jakarta.validation.Validation;
//import jakarta.validation.Validator;
//import jakarta.validation.ValidatorFactory;
//import org.junit.jupiter.api.AfterAll;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Tag;
//import org.junit.jupiter.api.Test;
//
//import java.util.Set;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
///**
// * CASO: Suite de validación de contrato — enabled es @NotNull en los tres DTOs de sub-recurso
// * existente (2.design.md § 3 regla #26). No es comportamiento de negocio, es forma pura.
// */
//@Tag("unit")
//class EnabledNotNullValidationTest {
//
//    private static ValidatorFactory validatorFactory;
//    private static Validator validator;
//
//    @BeforeAll
//    static void setUpValidator() {
//        validatorFactory = Validation.buildDefaultValidatorFactory();
//        validator = validatorFactory.getValidator();
//    }
//
//    @AfterAll
//    static void closeValidatorFactory() {
//        validatorFactory.close();
//    }
//
//    /**
//     * CASO: Instancia con enabled = null, resto de campos válidos.
//     * Validación: Un Validator de Bean Validation en aislamiento reporta violación sobre enabled (@NotNull).
//     */
//    @Test
//    void odontogramUpdateRequestDTO_enabledNull_failsValidation() {
//        OdontogramUpdateRequestDTO dto = new OdontogramUpdateRequestDTO(50L, Tooth.T11, ToothFace.TOP, 1L, 1L, null);
//
//        Set<ConstraintViolation<OdontogramUpdateRequestDTO>> violations = validator.validate(dto);
//
//        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("enabled"));
//    }
//
//    /**
//     * CASO: Instancia con id válido, enabled = null.
//     * Validación: Violación sobre enabled.
//     */
//    @Test
//    void prestationInstanceUpdateRequestDTO_enabledNull_failsValidation() {
//        PrestationInstanceUpdateRequestDTO dto = new PrestationInstanceUpdateRequestDTO(200L, null, null, null, null, null, null);
//
//        Set<ConstraintViolation<PrestationInstanceUpdateRequestDTO>> violations = validator.validate(dto);
//
//        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("enabled"));
//    }
//
//    /**
//     * CASO: Instancia con id/prestationInstanceId válidos, enabled = null.
//     * Validación: Violación sobre enabled.
//     */
//    @Test
//    void prestationStepUpdateRequestDTO_enabledNull_failsValidation() {
//        PrestationStepUpdateRequestDTO dto = new PrestationStepUpdateRequestDTO(200L, 300L, null, PrestationStepStatus.IN_PROGRESS, null);
//
//        Set<ConstraintViolation<PrestationStepUpdateRequestDTO>> violations = validator.validate(dto);
//
//        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("enabled"));
//    }
//}