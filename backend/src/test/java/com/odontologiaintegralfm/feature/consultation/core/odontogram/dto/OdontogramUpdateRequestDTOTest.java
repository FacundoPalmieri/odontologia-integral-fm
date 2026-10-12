//package com.odontologiaintegralfm.feature.consultation.core.odontogram.dto;
//
//import com.odontologiaintegralfm.feature.consultation.core.odontogram.enums.Tooth;
//import com.odontologiaintegralfm.feature.consultation.core.odontogram.enums.ToothFace;
//import org.junit.jupiter.api.Tag;
//import org.junit.jupiter.api.Test;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
///**
// * CASO: Suite del invariante de forma @AssertTrue en OdontogramUpdateRequestDTO — rechaza la
// * combinación alta+baja (id==null && enabled==false), regla #7 de 2.design.md.
// */
//@Tag("unit")
//class OdontogramUpdateRequestDTOTest {
//
//    /**
//     * CASO: id = null, enabled = false.
//     * Validación: El método anotado con @AssertTrue retorna false (Spring traduciría en 400).
//     */
//    @Test
//    void isNotContradictory_whenIdNullAndEnabledFalse_returnsFalse() {
//        OdontogramUpdateRequestDTO dto = new OdontogramUpdateRequestDTO(null, Tooth.T11, ToothFace.TOP, 1L, 1L, false);
//
//        assertThat(dto.isNotContradictory()).isFalse();
//    }
//
//    /**
//     * CASO: id = null, enabled = true.
//     * Validación: Retorna true — alta normal, sin contradicción.
//     */
//    @Test
//    void isNotContradictory_whenIdNullAndEnabledTrue_returnsTrue() {
//        OdontogramUpdateRequestDTO dto = new OdontogramUpdateRequestDTO(null, Tooth.T11, ToothFace.TOP, 1L, 1L, true);
//
//        assertThat(dto.isNotContradictory()).isTrue();
//    }
//
//    /**
//     * CASO: id con valor, enabled en cualquier valor (true/false).
//     * Validación: Retorna true — la regla de alta+baja solo aplica cuando id == null.
//     */
//    @Test
//    void isNotContradictory_whenIdPresent_returnsTrue() {
//        OdontogramUpdateRequestDTO dtoEnabled = new OdontogramUpdateRequestDTO(50L, Tooth.T11, ToothFace.TOP, 1L, 1L, true);
//        OdontogramUpdateRequestDTO dtoDisabled = new OdontogramUpdateRequestDTO(50L, Tooth.T11, ToothFace.TOP, 1L, 1L, false);
//
//        assertThat(dtoEnabled.isNotContradictory()).isTrue();
//        assertThat(dtoDisabled.isNotContradictory()).isTrue();
//    }
//}