//package com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto;
//
//import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.enums.PrestationStepStatus;
//import org.junit.jupiter.api.Tag;
//import org.junit.jupiter.api.Test;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
///**
// * CASO: Suite de los tres @AssertTrue de PrestationStepUpdateRequestDTO (2.design.md § 4/§ 3
// * reglas #7/#14): alta+baja combinada, status obligatorio cuando enabled==true, prestationStepId
// * obligatorio cuando id==null.
// */
//@Tag("unit")
//class PrestationStepUpdateRequestDTOTest {
//
//    // ─── isStatusDeclaredWhenEnabled ───────────────────────────────────────────
//
//    /**
//     * CASO: enabled = true, status = null.
//     * Validación: El método anotado con @AssertTrue retorna false — si el step sigue vigente, el
//     * front debe declarar su status real, no hay semántica de "null = sin cambios".
//     */
//    @Test
//    void isStatusDeclaredWhenEnabled_whenEnabledTrueAndStatusNull_returnsFalse() {
//        PrestationStepUpdateRequestDTO dto = new PrestationStepUpdateRequestDTO(200L, 300L, null, null, true);
//
//        assertThat(dto.isStatusDeclaredWhenEnabled()).isFalse();
//    }
//
//    /**
//     * CASO: enabled = true, status con valor.
//     * Validación: Retorna true.
//     */
//    @Test
//    void isStatusDeclaredWhenEnabled_whenEnabledTrueAndStatusPresent_returnsTrue() {
//        PrestationStepUpdateRequestDTO dto = new PrestationStepUpdateRequestDTO(200L, 300L, null, PrestationStepStatus.IN_PROGRESS, true);
//
//        assertThat(dto.isStatusDeclaredWhenEnabled()).isTrue();
//    }
//
//    /**
//     * CASO: enabled = false, status = null.
//     * Validación: Retorna true — al dar de baja, el valor de status es irrelevante.
//     */
//    @Test
//    void isStatusDeclaredWhenEnabled_whenEnabledFalseAndStatusNull_returnsTrue() {
//        PrestationStepUpdateRequestDTO dto = new PrestationStepUpdateRequestDTO(200L, 300L, null, null, false);
//
//        assertThat(dto.isStatusDeclaredWhenEnabled()).isTrue();
//    }
//
//    // ─── isNotContradictory (alta+baja) ────────────────────────────────────────
//
//    /**
//     * CASO: id = null (step nuevo), enabled = false.
//     * Validación: Retorna false — un step que nunca existió no puede nacer ya dado de baja (400).
//     */
//    @Test
//    void isNotContradictory_whenIdNullAndEnabledFalse_returnsFalse() {
//        PrestationStepUpdateRequestDTO dto = new PrestationStepUpdateRequestDTO(200L, null, 1L, null, false);
//
//        assertThat(dto.isNotContradictory()).isFalse();
//    }
//
//    /**
//     * CASO: id = null, enabled = true.
//     * Validación: Retorna true — alta normal de step sobre prestación existente.
//     */
//    @Test
//    void isNotContradictory_whenIdNullAndEnabledTrue_returnsTrue() {
//        PrestationStepUpdateRequestDTO dto = new PrestationStepUpdateRequestDTO(200L, null, 1L, PrestationStepStatus.IN_PROGRESS, true);
//
//        assertThat(dto.isNotContradictory()).isTrue();
//    }
//
//    /**
//     * CASO: id con valor, enabled en cualquier valor.
//     * Validación: Retorna true — la regla de alta+baja solo aplica cuando id == null.
//     */
//    @Test
//    void isNotContradictory_whenIdPresent_returnsTrue() {
//        PrestationStepUpdateRequestDTO dtoEnabled = new PrestationStepUpdateRequestDTO(200L, 300L, null, PrestationStepStatus.IN_PROGRESS, true);
//        PrestationStepUpdateRequestDTO dtoDisabled = new PrestationStepUpdateRequestDTO(200L, 300L, null, null, false);
//
//        assertThat(dtoEnabled.isNotContradictory()).isTrue();
//        assertThat(dtoDisabled.isNotContradictory()).isTrue();
//    }
//
//    // ─── isPrestationStepIdDeclaredWhenNew (hueco post-diseño) ─────────────────
//
//    /**
//     * CASO: id == null, prestationStepId == null.
//     * Validación: Retorna false — sin el id del catálogo, el backend no puede saber qué
//     * PrestationStep construir para un step nuevo (400).
//     */
//    @Test
//    void isPrestationStepIdDeclaredWhenNew_whenIdNullAndPrestationStepIdNull_returnsFalse() {
//        PrestationStepUpdateRequestDTO dto = new PrestationStepUpdateRequestDTO(200L, null, null, PrestationStepStatus.IN_PROGRESS, true);
//
//        assertThat(dto.isPrestationStepIdDeclaredWhenNew()).isFalse();
//    }
//
//    /**
//     * CASO: id == null, prestationStepId con valor.
//     * Validación: Retorna true.
//     */
//    @Test
//    void isPrestationStepIdDeclaredWhenNew_whenIdNullAndPrestationStepIdPresent_returnsTrue() {
//        PrestationStepUpdateRequestDTO dto = new PrestationStepUpdateRequestDTO(200L, null, 1L, PrestationStepStatus.IN_PROGRESS, true);
//
//        assertThat(dto.isPrestationStepIdDeclaredWhenNew()).isTrue();
//    }
//
//    /**
//     * CASO: id con valor, prestationStepId null.
//     * Validación: Retorna true — irrelevante cuando el step ya existe.
//     */
//    @Test
//    void isPrestationStepIdDeclaredWhenNew_whenIdPresent_returnsTrue() {
//        PrestationStepUpdateRequestDTO dto = new PrestationStepUpdateRequestDTO(200L, 300L, null, PrestationStepStatus.IN_PROGRESS, true);
//
//        assertThat(dto.isPrestationStepIdDeclaredWhenNew()).isTrue();
//    }
//}