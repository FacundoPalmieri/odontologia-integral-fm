package com.odontologiaintegralfm.feature.consultation.catalogs.enums;

/**
 * Enum que representa el estado de una prestación.
 */
public enum PrestationStatus {
    IN_PROGRESS("En progreso"),
    COMPLETED ("Finalizada"),
    CANCELLED("Cancelada");

    private String label;

    PrestationStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

