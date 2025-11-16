package com.odontologiaintegralfm.feature.appointment.catalogs.enums;

import lombok.Getter;

import java.time.DayOfWeek;

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

    public static DayName fromDayOfWeek(DayOfWeek dayOfWeek) {
        return DayName.valueOf(dayOfWeek.name());
    }

    public DayOfWeek toDayOfWeek() {
        return switch (this) {
            case MONDAY -> DayOfWeek.MONDAY;
            case TUESDAY -> DayOfWeek.TUESDAY;
            case WEDNESDAY -> DayOfWeek.WEDNESDAY;
            case THURSDAY -> DayOfWeek.THURSDAY;
            case FRIDAY -> DayOfWeek.FRIDAY;
            case SATURDAY -> DayOfWeek.SATURDAY;
            case SUNDAY -> DayOfWeek.SUNDAY;
        };
    }


}
