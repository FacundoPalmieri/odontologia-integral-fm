package com.odontologiaintegralfm.enums;

import lombok.Getter;

/**
 * Tipo de información brindada al día para la vista mensual.
 */
@Getter
public enum CalendarMonthStatus {
    AVAILABLE("Disponible", "#4CAF50"),   // verde
    HOLIDAY("Feriado", "#F44336"),       // rojo
    BLOCKED("Bloqueo de agenda", "#FF9800"), // naranja
    FULL("Sin turnos disponibles", "#9E9E9E"); // gris

    private final String description;
    private final String colorHex;

    CalendarMonthStatus(String description, String colorHex) {
        this.description = description;
        this.colorHex = colorHex;
    }
}
