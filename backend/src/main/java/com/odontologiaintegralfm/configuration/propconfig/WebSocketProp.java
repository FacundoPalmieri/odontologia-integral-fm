package com.odontologiaintegralfm.configuration.propconfig;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
@ConfigurationProperties(prefix = "websocket")
public class WebSocketProp {

    // Endpoint principal donde los clientes se conectan al WebSocket
    // Ej: /ws
    private String endpoint;

    // Prefijo de los mensajes que el cliente envía al servidor
    // Ej: /app
    private String appPrefix;

    // Prefijo de los tópicos a los que los clientes se suscriben
    // Ej: /topic -> el servidor envía mensajes a /topic/consultas
    private String brokerPrefix;

    // Orígenes permitidos (CORS)
    private String allowedOrigins;

    // Indica si se debe usar SockJS cuando el navegador no soporta WebSocket nativo
    private boolean useSockjs;

}
