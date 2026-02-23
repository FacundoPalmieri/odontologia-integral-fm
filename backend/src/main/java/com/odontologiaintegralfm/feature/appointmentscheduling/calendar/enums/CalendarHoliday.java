package com.odontologiaintegralfm.feature.appointmentscheduling.calendar.enums;

import lombok.Getter;

/**
 * Enum que representa la vista de un feriado en el calendario
 */
@Getter
public enum CalendarHoliday {
    HOLIDAY("Feriado", "#48925f");

    private final String label;
    private final String colorHex;

    CalendarHoliday(String label, String colorHex) {
        this.label = label;
        this.colorHex = colorHex;
    }
}
