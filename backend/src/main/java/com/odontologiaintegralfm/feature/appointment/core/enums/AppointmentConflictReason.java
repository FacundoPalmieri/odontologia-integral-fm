package com.odontologiaintegralfm.feature.appointment.core.enums;

public enum AppointmentConflictReason {
    OUT_OF_SCHEDULE("Fuera del horario de la jornada del dentista"),
    OVERLAPPING_SLOT("Solapamiento de turnos"),
    DOUBLE_BOOKED_PATIENT("Paciente con turno duplicado"),
    DENTIST_UNAVAILABLE("Dentista no disponible");

    private final String label;

    AppointmentConflictReason(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
