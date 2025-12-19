package com.odontologiaintegralfm.feature.appointment.core.enums;

import lombok.Getter;

/**
 * Estado de un turno.
 */
@Getter
public enum AppointmentStatus {
    RESERVED("Reservado", "#4b99d2"),     // celeste
    ATTENDED("Atendido", "#28a745"),     // verde éxito
    NO_SHOW("Ausente", "#dc3545"),       // rojo error
    RESCHEDULED("Reprogramado", "#fd7e14"), //SOLO PARA HISTORIAL
    CANCELED("Cancelado", "#343a40");    // gris oscuro / anulado


    private final String label;
    private final String color;

    AppointmentStatus(String label, String color) {
        this.label = label;
        this.color = color;
    }
}
