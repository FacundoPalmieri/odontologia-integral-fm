package com.odontologiaintegralfm.feature.appointment.core.enums;

import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.BadRequestException;
import lombok.Getter;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

/**
 * Representa los tipos de recurrencia en eventos de bloqueo de agenda.
 */
@Getter
public enum CalendarLockRecurrenceName {

    DAILY("Diario") {
        @Override
        public boolean matches(LocalDate startDate, LocalDate currentDate) {

            return !currentDate.isBefore(startDate);
        }
    },

    // Día de inicio y su repetición semanal (EJ. todos los jueves)
    WEEKLY("Semanal") {
        @Override
        public boolean matches(LocalDate startDate, LocalDate currentDate) {
            return startDate.getDayOfWeek() == currentDate.getDayOfWeek();
        }
    },

    // Día de inicio y su repetición quincenal (EJ. Jueves por medio)
    BIWEEKLY("Quincenal") {
        @Override
        public boolean matches(LocalDate startDate, LocalDate currentDate) {
            long weeks = ChronoUnit.WEEKS.between(startDate, currentDate);
            return startDate.getDayOfWeek() == currentDate.getDayOfWeek()
                    && weeks % 2 == 0;
        }
    },

    // Por Número de día (Ej. Segundo martes del mes)
    MONTHLY("Mensual") {
        @Override
        public boolean matches(LocalDate startDate, LocalDate currentDate) {
            int startWeek = (startDate.getDayOfMonth() - 1) / 7;
            int currentWeek = (currentDate.getDayOfMonth() - 1) / 7;
            return startDate.getDayOfWeek() == currentDate.getDayOfWeek()
                    && startWeek == currentWeek;
        }
    },

    // Fecha fija por año (Mismo número de día)
    YEARLY("Anual") {
        @Override
        public boolean matches(LocalDate startDate, LocalDate currentDate) {
            return startDate.getMonth() == currentDate.getMonth()
                    && startDate.getDayOfMonth() == currentDate.getDayOfMonth();
        }
    },
    NONE("Sin repetición") {
        @Override
        public boolean matches(LocalDate startDate, LocalDate currentDate) {
            return startDate.equals(currentDate);
        }
    };

    public abstract boolean matches(LocalDate startDate, LocalDate currentDate);

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
