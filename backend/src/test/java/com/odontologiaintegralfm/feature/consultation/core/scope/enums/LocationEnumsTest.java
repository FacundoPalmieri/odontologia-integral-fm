package com.odontologiaintegralfm.feature.consultation.core.scope.enums;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class LocationEnumsTest {

    /** Oráculo independiente del código FDI: primer dígito → cuadrante. */
    private static Quadrant expectedQuadrant(Tooth tooth) {
        return switch (tooth.getCode() / 10) {
            case 1 -> Quadrant.UPPER_RIGHT;
            case 2 -> Quadrant.UPPER_LEFT;
            case 3 -> Quadrant.LOWER_LEFT;
            case 4 -> Quadrant.LOWER_RIGHT;
            case 5 -> Quadrant.UPPER_RIGHT_TEMPORARY;
            case 6 -> Quadrant.UPPER_LEFT_TEMPORARY;
            case 7 -> Quadrant.LOWER_LEFT_TEMPORARY;
            case 8 -> Quadrant.LOWER_RIGHT_TEMPORARY;
            default -> throw new IllegalStateException("Código FDI inesperado: " + tooth.getCode());
        };
    }

    /**
     * CASO: Cada uno de los 52 dientes del enum se asigna a su cuadrante.
     * Regla: El primer dígito del código FDI define el cuadrante (1-4 permanentes, 5-8 temporales).
     * Validación: 11-18 → UPPER_RIGHT, 21-28 → UPPER_LEFT, 31-38 → LOWER_LEFT, 41-48 → LOWER_RIGHT,
     * 51-55 → UPPER_RIGHT_TEMPORARY, 61-65 → UPPER_LEFT_TEMPORARY, 71-75 → LOWER_LEFT_TEMPORARY, 81-85 → LOWER_RIGHT_TEMPORARY.
     */
    @ParameterizedTest
    @EnumSource(Tooth.class)
    void quadrantFromTooth_everyTooth_returnsItsQuadrant(Tooth tooth) {
        assertThat(Quadrant.fromTooth(tooth)).isEqualTo(expectedQuadrant(tooth));
    }

    /**
     * CASO: Cada uno de los 52 dientes del enum se asigna a su maxilar.
     * Regla: Códigos 1x, 2x, 5x, 6x son del maxilar superior; 3x, 4x, 7x, 8x del inferior.
     * Validación: Maxillary.fromTooth devuelve UPPER o LOWER según ese criterio.
     */
    @ParameterizedTest
    @EnumSource(Tooth.class)
    void maxillaryFromTooth_everyTooth_returnsItsMaxillary(Tooth tooth) {
        int first = tooth.getCode() / 10;
        Maxillary expected = (first == 1 || first == 2 || first == 5 || first == 6) ? Maxillary.UPPER : Maxillary.LOWER;

        assertThat(Maxillary.fromTooth(tooth)).isEqualTo(expected);
    }

    /**
     * CASO: Cantidad de dientes por cuadrante.
     * Regla: Un cuadrante permanente tiene 8 dientes; uno temporal, 5.
     * Validación: getTeeth() contiene exactamente (containsExactlyInAnyOrder) los códigos FDI del cuadrante
     * (UPPER_RIGHT = 11..18, UPPER_RIGHT_TEMPORARY = 51..55, etc.) y todos los dientes devueltos cumplen
     * fromTooth = ese cuadrante. Una lista con un diente repetido (8 elementos, 7 distintos) falla por contenido.
     */
    @Test
    void quadrantGetTeeth_permanentHasEightTemporaryHasFive() {
        for (Quadrant quadrant : Quadrant.values()) {
            assertThat(quadrant.getTeeth()).as(quadrant.name())
                    .extracting(Tooth::getCode)
                    .containsExactlyInAnyOrder(expectedCodes(quadrant));
            assertThat(quadrant.getTeeth()).as(quadrant.name())
                    .allSatisfy(tooth -> assertThat(Quadrant.fromTooth(tooth)).isEqualTo(quadrant));
        }
    }

    /** Oráculo independiente: códigos FDI de cada cuadrante (permanentes x1..x8, temporales x1..x5). */
    private static Integer[] expectedCodes(Quadrant quadrant) {
        return switch (quadrant) {
            case UPPER_RIGHT -> codes(10, 8);
            case UPPER_LEFT -> codes(20, 8);
            case LOWER_LEFT -> codes(30, 8);
            case LOWER_RIGHT -> codes(40, 8);
            case UPPER_RIGHT_TEMPORARY -> codes(50, 5);
            case UPPER_LEFT_TEMPORARY -> codes(60, 5);
            case LOWER_LEFT_TEMPORARY -> codes(70, 5);
            case LOWER_RIGHT_TEMPORARY -> codes(80, 5);
        };
    }

    private static Integer[] codes(int base, int count) {
        Integer[] result = new Integer[count];
        for (int i = 0; i < count; i++) {
            result[i] = base + i + 1;
        }
        return result;
    }

    /**
     * CASO: Dientes de cada maxilar.
     * Regla: Un maxilar tiene 26 dientes (16 permanentes + 10 temporales).
     * Validación: UPPER y LOWER devuelven 26 dientes cada uno, sin intersección entre los dos.
     */
    @Test
    void maxillaryGetTeeth_returnsAllTeethOfThatMaxillary() {
        Set<Tooth> upper = new HashSet<>(Maxillary.UPPER.getTeeth());
        Set<Tooth> lower = new HashSet<>(Maxillary.LOWER.getTeeth());

        assertThat(upper).hasSize(26);
        assertThat(lower).hasSize(26);
        assertThat(upper).doesNotContainAnyElementsOf(lower);
        assertThat(upper).allSatisfy(t -> assertThat(Maxillary.fromTooth(t)).isEqualTo(Maxillary.UPPER));
        assertThat(lower).allSatisfy(t -> assertThat(Maxillary.fromTooth(t)).isEqualTo(Maxillary.LOWER));
        assertThat(upper.size() + lower.size()).isEqualTo(Arrays.stream(Tooth.values()).count());
    }
}
