package com.odontologiaintegralfm.enums;

import lombok.Getter;

/**
 * Representa los días semanales.
 */
@Getter
public enum DayName {
    LUNES("Lunes"),
    MARTES("Martes"),
    MIERCOLES("Miércoles"),
    JUEVES("Jueves"),
    VIERNES("Viernes"),
    SABADO("Sábado"),
    DOMINGO("Domingo");

    private final String label;

    DayName(String label) {
        this.label = label;
    }

    public String getLabel() { return label; }
}
