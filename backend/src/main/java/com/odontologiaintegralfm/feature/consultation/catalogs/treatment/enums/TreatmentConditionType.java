package com.odontologiaintegralfm.feature.consultation.catalogs.treatment.enums;

/**
 * Representa el código de cada fila real de la tabla treatment_condition (verificado contra
 * las 4 filas existentes). Requiere que el dev normalice previamente la columna name de esas
 * filas a estos mismos valores antes de desplegar (REQ-021, fuera de su alcance).
 */
public enum TreatmentConditionType {
    PREEXISTING,
    REQUIRED,
    IN_PROGRESS,
    COMPLETED
}