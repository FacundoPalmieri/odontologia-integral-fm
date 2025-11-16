package com.odontologiaintegralfm.feature.appointment.core.enums;


public enum OriginConflict {
    DENTIST_CALENDAR_LOCK("Evento de bloqueo de calendario."),
    DENTIST_AVAILABILITIES("Fuera de la jornada laboral.");

    private String label;

    OriginConflict(String label) {
        this.label = label;
    }

    public String getLabel(){
        return label;
    }

}
