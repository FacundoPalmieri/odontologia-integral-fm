package com.odontologiaintegralfm.feature.appointment.enums;

import lombok.Getter;

/**
 * Tipos de feriados.
 * Los mismos son tomados del servicio:
 * https://api.argentinadatos.com/v1/feriados/
 *
 */
@Getter
public enum HolidayType {

    IMMOVABLE("Inamovible"),
    MOVEABLE("Trasladable"),
    LONG_WEEKEND("Puente");

    private final String label;

    HolidayType(String label){
        this.label = label;
    }
}
