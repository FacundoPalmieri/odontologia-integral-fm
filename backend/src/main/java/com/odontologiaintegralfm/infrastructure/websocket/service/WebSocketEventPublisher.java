package com.odontologiaintegralfm.infrastructure.websocket.service;

import com.odontologiaintegralfm.infrastructure.websocket.dto.WebSocketMessage;
import com.odontologiaintegralfm.infrastructure.websocket.enums.WebSocketEventType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class WebSocketEventPublisher implements IWebSocketEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;


    public WebSocketEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }


    /**
     * @param type
     * @param data
     * @param <T>
     */
    @Override
    public <T> void publish(WebSocketEventType type, T data) {
        WebSocketMessage<T> message = new WebSocketMessage<>(
                type,
                LocalDateTime.now(),
                data
        );

        messagingTemplate.convertAndSend(WebSocketDestinations.CONSULTATIONS, message);
    }
}
