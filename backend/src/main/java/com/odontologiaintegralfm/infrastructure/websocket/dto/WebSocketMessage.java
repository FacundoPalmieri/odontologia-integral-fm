package com.odontologiaintegralfm.infrastructure.websocket.dto;


import com.odontologiaintegralfm.infrastructure.websocket.enums.WebSocketEventType;

import java.time.LocalDateTime;

public record WebSocketMessage<T>(
        WebSocketEventType type,
        LocalDateTime timestamp,
        T data
) {
}
