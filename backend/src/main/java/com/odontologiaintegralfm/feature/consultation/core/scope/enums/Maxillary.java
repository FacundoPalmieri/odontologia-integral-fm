package com.odontologiaintegralfm.feature.consultation.core.scope.enums;

import java.util.Arrays;
import java.util.List;

/**
 * Enum que representa los maxilares.
 */
public enum Maxillary {
    UPPER ("Maxilar Superior"),
    LOWER("Maxilar Inferior");

    private String label;

    Maxillary(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** Todos los dientes del maxilar: 16 permanentes y 10 temporales. */
    public List<Tooth> getTeeth() {
        return Arrays.stream(Tooth.values())
                .filter(tooth -> fromTooth(tooth) == this)
                .toList();
    }

    /** Maxilar al que pertenece un diente: cuadrantes 1, 2, 5 y 6 son superiores; 3, 4, 7 y 8, inferiores. */
    public static Maxillary fromTooth(Tooth tooth) {
        int digit = tooth.getCode() / 10;
        return (digit == 1 || digit == 2 || digit == 5 || digit == 6) ? UPPER : LOWER;
    }
}

