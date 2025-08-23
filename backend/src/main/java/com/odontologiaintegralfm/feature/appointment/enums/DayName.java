package com.odontologiaintegralfm.feature.appointment.enums;

import lombok.Getter;

/**
 * Representa los días semanales.
 */
@Getter
public enum DayName {
    MONDAY("Lunes"),
    TUESDAY("Martes"),
    WEDNESDAY("Miércoles"),
    THURSDAY("Jueves"),
    FRIDAY("Viernes"),
    SATURDAY("Sábado"),
    SUNDAY("Domingo");

    private final String label;

    DayName(String label) {
        this.label = label;
    }

    public String getLabel() { return label; }
}
