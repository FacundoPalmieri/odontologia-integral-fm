package com.odontologiaintegralfm.feature.consultation.core.odontogram.enums;

/**
 * Enum que representa los cuadrantes de la dentadura.
 */
public enum Quadrant {
    UPPER_RIGHT("Cuadrante Superior Derecho"),
    UPPER_LEFT("Cuadrante Superior Izquierdo"),
    LOWER_RIGHT("Cuadrante Inferior Derecho"),
    LOWER_LEFT("Cuadrante Inferior Izquierdo");

    private final String label;

    Quadrant(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

