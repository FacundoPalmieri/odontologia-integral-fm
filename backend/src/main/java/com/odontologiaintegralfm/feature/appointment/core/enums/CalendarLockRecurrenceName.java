package com.odontologiaintegralfm.feature.appointment.core.enums;

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
    DAILY("Diario"),        // Todos los días
    WEEKLY("Semanal"),      // Día de inicio y su repetición semanal (EJ. todos los jueves)
    BIWEEKLY ("Quincenal"), // Día de inicio y su repetición quincenal (EJ. Jueves por medio)
    MONTHLY("Mensual"),     // Por Número de día (Ej. Segundo martes del mes)
    YEARLY("Anual");        // Fecha fija por año (Mismo número de día)

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
