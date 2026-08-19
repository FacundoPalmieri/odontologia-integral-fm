package com.odontologiaintegralfm.feature.consultation.catalogs.promotion.enums;

/**
 * Estado derivado de una promoción a partir de sus fechas persistidas (ADR-0025).
 * No se persiste — se calcula en el momento de lectura.
 */
public enum PromotionStatus {
    NOT_STARTED("No iniciada"),
    ACTIVE("Vigente"),
    FINISHED("Finalizada");

    private final String label;

    PromotionStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
