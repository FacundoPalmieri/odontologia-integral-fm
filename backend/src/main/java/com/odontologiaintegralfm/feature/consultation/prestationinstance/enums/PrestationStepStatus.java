package com.odontologiaintegralfm.feature.consultation.prestationinstance.enums;

/**
 * Enum que representa estado de pasos de una prestación.
 */
public enum PrestationStepStatus {
    IN_PROGRESS("En progreso"),
    FINISHED("Finalizado"),
    OMITTED("Omitido"),
    REPLACED("Reemplazado"),
    CANCELED("Cancelado");

    private String label;

    PrestationStepStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
