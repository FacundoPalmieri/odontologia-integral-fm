package com.odontologiaintegralfm.feature.consultation.catalogs.enums;

/**
 * @author [Facundo Palmieri]
 */

public enum ProviderType {
    BANK("Banco", "CBU"),
    DIGITAL_WALLET("Billetera virtual", "CVU");

    private String name;
    private String identifier;

    ProviderType(String name, String identifier) {
        this.name = name;
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public static ProviderType fromName(String name) {
        for (ProviderType type : ProviderType.values()) {
            if (type.getName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Tipo de proveedor desconocido: " + name);
    }

}

