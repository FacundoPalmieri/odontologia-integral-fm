package com.odontologiaintegralfm.feature.consultation.core.scope.enums;

import java.util.Arrays;
import java.util.List;

/**
 * Enum que representa los cuadrantes de la dentadura.
 */
public enum Quadrant {
    UPPER_RIGHT("Cuadrante Superior Derecho"),
    UPPER_LEFT("Cuadrante Superior Izquierdo"),
    LOWER_RIGHT("Cuadrante Inferior Derecho"),
    LOWER_LEFT("Cuadrante Inferior Izquierdo"),

    UPPER_RIGHT_TEMPORARY("Cuadrante Superior Derecho Temporal"),
    UPPER_LEFT_TEMPORARY("Cuadrante Superior Izquierdo Temporal"),
    LOWER_LEFT_TEMPORARY("Cuadrante Inferior Izquierdo Temporal"),
    LOWER_RIGHT_TEMPORARY("Cuadrante Inferior Derecho Temporal");

    private final String label;

    Quadrant(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** Primer dígito FDI de los dientes del cuadrante (1..4 permanentes, 5..8 temporales). */
    private int fdiDigit() {
        return switch (this) {
            case UPPER_RIGHT -> 1;
            case UPPER_LEFT -> 2;
            case LOWER_LEFT -> 3;
            case LOWER_RIGHT -> 4;
            case UPPER_RIGHT_TEMPORARY -> 5;
            case UPPER_LEFT_TEMPORARY -> 6;
            case LOWER_LEFT_TEMPORARY -> 7;
            case LOWER_RIGHT_TEMPORARY -> 8;
        };
    }

    /** Dientes del cuadrante, en el orden del enum {@link Tooth}. */
    public List<Tooth> getTeeth() {
        int digit = fdiDigit();
        return Arrays.stream(Tooth.values())
                .filter(tooth -> tooth.getCode() / 10 == digit)
                .toList();
    }

    /** Cuadrante al que pertenece un diente, según el primer dígito de su código FDI. */
    public static Quadrant fromTooth(Tooth tooth) {
        int digit = tooth.getCode() / 10;
        for (Quadrant quadrant : values()) {
            if (quadrant.fdiDigit() == digit) {
                return quadrant;
            }
        }
        throw new IllegalArgumentException("Diente sin cuadrante: " + tooth);
    }
}

