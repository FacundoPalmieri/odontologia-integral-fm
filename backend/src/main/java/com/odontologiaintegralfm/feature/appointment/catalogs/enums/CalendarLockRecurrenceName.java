package com.odontologiaintegralfm.feature.appointment.catalogs.enums;

import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.BadRequestException;
import lombok.Getter;

import java.util.Arrays;

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


    public static CalendarLockRecurrenceName fromString(String value) {
        return Arrays.stream(CalendarLockRecurrenceName.values())
                .filter(calendarLockRecurrenceName -> calendarLockRecurrenceName.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(()-> new BadRequestException("exception.calendarLockRecurrenceName.user",null,"exception.calendarLockRecurrenceName.log",new Object[]{value,"Enum: CalendarLockRecurrenceName","fromString"}, LogLevel.ERROR));
    }


}
