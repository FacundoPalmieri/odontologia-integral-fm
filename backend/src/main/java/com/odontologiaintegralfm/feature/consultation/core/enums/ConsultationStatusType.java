package com.odontologiaintegralfm.feature.consultation.core.enums;

import lombok.Getter;


@Getter
public enum ConsultationStatusType {
    WAITING_ROOM ("Sala de Espera"),
    IN_CONSULTATION("En Atención"),
    PENDING_PAYMENT("Pendiente de Pago"),
    FINISHED("Finalizada");


    private final String label;

    ConsultationStatusType(String label) {
        this.label = label;
    }


    public boolean isAfter(ConsultationStatusType other) {

        return other.ordinal() > this.ordinal();
    }
}
