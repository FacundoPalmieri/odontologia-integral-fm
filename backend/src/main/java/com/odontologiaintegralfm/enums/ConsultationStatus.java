package com.odontologiaintegralfm.enums;

import lombok.Getter;


@Getter
public enum ConsultationStatus {
    IN_WAITING_ROOM ("Sala de Espera"),   // EN_SALA_ESPERA
    IN_PROGRESS("En Atención"),       // EN_ATENCION
    PENDING_PAYMENT("Pendiente de Pago"),   // PENDIENTE_PAGO
    COMPLETED("Finalizada");// FINALIZADA

    private final String label;

    ConsultationStatus(String label) {
        this.label = label;
    }
}
