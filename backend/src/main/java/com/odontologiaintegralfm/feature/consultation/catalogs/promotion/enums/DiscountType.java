package com.odontologiaintegralfm.feature.consultation.catalogs.enums;

/**
 * Enum que representa el tipo de descuento a aplicar.
 */

public enum DiscountType {
    FIXED ("Monto Fijo"),
    PERCENTAGE ("Porcentaje");



    private final String label;

    DiscountType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

