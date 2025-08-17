package com.odontologiaintegralfm.enums;

import lombok.Getter;

/**
 * Estado de un turno.
 */
@Getter
public enum AppointmentStatus {
    RESERVED("Reservado"),
    ATTENDED("Atendido"),
    NO_SHOW("Ausente"),
    RESCHEDULED("Reprogramado"),
    CANCELED("Cancelado");


    private final String label;

    AppointmentStatus(String label){
        this.label = label;
    }
}
