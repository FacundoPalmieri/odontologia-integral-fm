package com.odontologiaintegralfm.infrastructure.websocket.service;

import com.odontologiaintegralfm.infrastructure.websocket.enums.WebSocketEventType;

import java.time.LocalDateTime;


public interface IWebSocketEventPublisher {
    <T> void publish(WebSocketEventType type, T data);
}
