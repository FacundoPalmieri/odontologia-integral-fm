package com.odontologiaintegralfm.feature.appointment.core.enums;

import lombok.Getter;

/**
 * Tipo de información brindada para marcar el cada día en la vista mensual.
 */
@Getter
public enum CalendarDayStatus {
    FREE("Disponible", "#4CAF50"),   // verde
    LOCKED("Bloqueo de agenda", "#FF9800"), // naranja
    FULL("Sin turnos disponibles", "#B0BEC5"), // gris
    NOT_AVAILABLE("No trabaja","#9E9E9E");



    private final String description;
    private final String colorHex;

    CalendarDayStatus(String description, String colorHex) {
        this.description = description;
        this.colorHex = colorHex;
    }
}
