package com.odontologiaintegralfm.feature.consultation.catalogs.treatment.enums;

/**
 * Condiciones que admite un tratamiento. Cada clave es una condición posible de {@code Treatment.conditions}.
 */
public enum TreatmentConditionType {
    PREEXISTING,
    REQUIRED,
    IN_PROGRESS,
    FINISHED,
    REPLACED,
    CANCELED
}