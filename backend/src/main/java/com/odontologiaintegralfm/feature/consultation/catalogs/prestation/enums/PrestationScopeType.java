package com.odontologiaintegralfm.feature.consultation.catalogs.prestation.enums;

/**
 * Enum que representa el alcance de una prestación sin ubicación específica.
 */
public enum PrestationScopeType {
    TOOTH ("Diente"),
    TOOTH_FACE ("Diente y Cara"),
    QUADRANT ("Cuadrante"),
    MAXILLARY ("Maxilar"),
    FULL_MOUTH ("Boca Completa");

    private String label;

    PrestationScopeType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

