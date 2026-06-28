package com.odontologiaintegralfm.feature.consultation.catalogs.enums;

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
}

