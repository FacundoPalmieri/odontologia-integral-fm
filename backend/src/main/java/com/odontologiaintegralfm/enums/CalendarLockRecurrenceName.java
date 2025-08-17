package com.odontologiaintegralfm.enums;

import lombok.Getter;

/**
 * Representa los tipos de recurrencia en eventos de bloqueo de agenda.
 */
@Getter
public enum CalendarLockRecurrenceName {
    NONE("Sin repetición"),
    DAILY("Diario"), //Bloquea todos los días
    WEEKLY("Semanal"), // Bloquea el día de inicio y su repetición semanal (EJ. todos los jueves)
    MONTHLY("Mensual"), //Bloquea por Número de día (Ej todos los 21)
    YEARLY("Anual"); // Bloquea cada fecha fija por año (Cumpleaños)

    private final String label;
    CalendarLockRecurrenceName(String label) {
        this.label = label;
    }
}
