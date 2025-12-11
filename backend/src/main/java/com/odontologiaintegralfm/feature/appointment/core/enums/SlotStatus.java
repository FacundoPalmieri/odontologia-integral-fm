package com.odontologiaintegralfm.feature.appointment.core.enums;

import lombok.Getter;

/**
 * Tipo de información brindada al día para la vista mensual.
 */
@Getter
public enum SlotStatus {
    FREE("Disponible", "#4CAF50"),   // verde
    RESERVED("Reservado", "#F44336"),       // rojo
    LOCKED("Bloqueo de agenda", "#FF9800");// naranja


    private final String description;
    private final String colorHex;

    SlotStatus(String description, String colorHex) {
        this.description = description;
        this.colorHex = colorHex;
    }
}
