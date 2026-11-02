package com.odontologiaintegralfm.feature.consultation.prestationinstance.enums;

/**
 * Enum que representa el estado de una prestación (Agrupador de pasos)
 */
public enum PrestationInstanceStatus {
    IN_PROGRESS("En progreso"),
    FINISHED("Finalizada"),
    REPLACED("Reemplazada"),
    CANCELED("Cancelada");

    private String label;

    PrestationInstanceStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

