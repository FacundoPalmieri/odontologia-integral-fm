package com.odontologiaintegralfm.feature.consultation.catalogs.enums;

/**
 * @author [Facundo Palmieri]
 */
public enum PaymentMethods {
    CASH("Efectivo"),
    TRANSFER("Transferencia");

    private String displayName;

    PaymentMethods(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
