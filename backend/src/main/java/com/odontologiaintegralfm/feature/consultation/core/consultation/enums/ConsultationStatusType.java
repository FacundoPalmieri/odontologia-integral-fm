package com.odontologiaintegralfm.feature.consultation.core.consultation.enums;

import com.odontologiaintegralfm.infrastructure.websocket.enums.WebSocketEventType;
import lombok.Getter;


@Getter
public enum ConsultationStatusType {
    WAITING_ROOM ("Sala de Espera"){
        @Override
        public ConsultationStatusType next() {
            return ConsultationStatusType.IN_CONSULTATION;
        }



        @Override
        public WebSocketEventType webSocketEvent() {
            return WebSocketEventType.PATIENT_RECEIVED;
        }
    },


    IN_CONSULTATION("En Atención"){
        @Override
        public ConsultationStatusType next() {
            return ConsultationStatusType.FINISHED;
        }

        @Override
        public ConsultationStatusType previous() {
            return ConsultationStatusType.WAITING_ROOM;
        }


        @Override
        public WebSocketEventType webSocketEvent() {
            return WebSocketEventType.ATTENTION_STARTED;
        }
    },

    FINISHED("Finalizada"){
        @Override
        public ConsultationStatusType next() {
            return ConsultationStatusType.FINISHED;
        }


        @Override
        public WebSocketEventType webSocketEvent() {
            return WebSocketEventType.ATTENTION_FINISHED;
        }
    };




    private final String label;

    ConsultationStatusType(String label) {
        this.label = label;
    }




    public abstract ConsultationStatusType next();


    //Me pide una implementación por default para los casos del enum que no sobreescriben el método.
    public ConsultationStatusType previous() {
        return null;
    }

    public abstract WebSocketEventType webSocketEvent();

}
