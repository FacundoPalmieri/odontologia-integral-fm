package com.odontologiaintegralfm.feature.consultation.core.prestationinstance.enums;

/**
 * Enum que representa estado de pasos de una prestación.
 */
public enum PrestationStepStatus {
    IN_PROGRESS("En progreso"),
    COMPLETED ("Finalizada"),
    CANCELLED("Cancelada"),
    OMITTED("Omitido");

    private String label;

    PrestationStepStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
