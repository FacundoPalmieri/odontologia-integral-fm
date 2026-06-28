package com.odontologiaintegralfm.feature.consultation.core.odontogram.enums;

/**
 * Enum que representa la cara del diente
 */
public enum ToothFace {

    TOP("Cara Superior"),
    BOTTOM("Cara Inferior"),
    RIGHT("Cara Derecha"),
    LEFT("Cara Izquierda"),
    CENTER("Centro del Diente");

    private final String label;

    ToothFace(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }


}
