package com.odontologiaintegralfm.feature.consultation.core.enums;

import lombok.Getter;


@Getter
public enum ConsultationStatus {
    WAITING_ROOM ("Sala de Espera"),
    IN_CONSULTATION("En Atención"),
    PENDING_PAYMENT("Pendiente de Pago"),
    FINISHED("Finalizada");
    private final String label;

    ConsultationStatus(String label) {
        this.label = label;
    }
}
