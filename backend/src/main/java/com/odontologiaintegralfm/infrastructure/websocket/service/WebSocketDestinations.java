package com.odontologiaintegralfm.infrastructure.websocket.service;


import org.springframework.stereotype.Component;

@Component
public class WebSocketDestinations {
    public static final String CONSULTATIONS = "/topic/consultations";
    public static final String PAYMENTS = "/topic/payments";
    public static final String CHAT = "/topic/chat";
}
