package com.odontologiaintegralfm.feature.appointmentscheduling.holiday.enums;

import lombok.Getter;

import java.util.Arrays;

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

    public static HolidayType fromLabel(String label){
        return Arrays.stream(values())
                .filter(e -> e.label.equalsIgnoreCase(label))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tipo de feriado inválido: " + label
                ));
    }
}
