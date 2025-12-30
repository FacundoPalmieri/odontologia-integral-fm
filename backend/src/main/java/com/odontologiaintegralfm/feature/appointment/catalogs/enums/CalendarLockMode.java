package com.odontologiaintegralfm.feature.appointment.catalogs.enums;

import lombok.Getter;


import java.util.Set;

/**
 * Enum que identifica los modos de bloqueo para un evento.
 */


@Getter
public enum CalendarLockMode {

    POINTUAL(
            "Bloqueo puntual",
            "Bloquea una única con fecha inicio/fin iguales, sin recurrencia y sin especificación de días."
    ),

    DAYS_IN_RANGE_NO_RECURRENCE(
            "Días discontinuos sin recurrencia",
            "Bloquea días seleccionados dentro de un rango sin patrón de repetición"
    ),

    DAILY_CONTINUOUS(
            "Días continuos",
            "Bloquea todos los días entre la fecha de inicio y fin, solo con recurrencia 'Diaria' y sin especificación de días."
    ),

    RECURRENT_PATTERN(
            "Días con recurrencia",
            "Bloquea días fijos respetando una recurrencia semanal, mensual o anual, con fecha de inicio/fin diferentes"
    );

    private final String label;
    private final String description;

    CalendarLockMode(String label, String description) {
        this.label = label;
        this.description = description;
    }


    public static Set<CalendarLockMode> getAll() {
        return Set.of(CalendarLockMode.values());
    }


}
