package com.odontologiaintegralfm.feature.appointmentscheduling.shared;

import lombok.Getter;

import java.time.DayOfWeek;
import java.util.Collections;
import java.util.List;

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


    //Convierte lista de DayName a DayOfWeek
    public  static List<DayOfWeek> toDayOfWeek(List<DayName> days){
        if (days == null) return Collections.emptyList();
        return days.stream()
                .map(DayName::toDayOfWeek)
                .toList();
    }


    public static List<DayName> listDayName(){
        return List.of(
                DayName.SUNDAY,
                DayName.MONDAY,
                DayName.TUESDAY,
                DayName.WEDNESDAY,
                DayName.THURSDAY,
                DayName.FRIDAY,
                DayName.SATURDAY
        );
    }

}
