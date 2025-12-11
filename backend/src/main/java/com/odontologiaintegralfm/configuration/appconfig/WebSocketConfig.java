package com.odontologiaintegralfm.configuration.appconfig;


import com.odontologiaintegralfm.configuration.propconfig.WebSocketProp;
import com.odontologiaintegralfm.configuration.securityconfig.websocket.JwtHandshakeInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


/**
 * Configuración principal de WebSocket para la aplicación.
 *
 * <p>
 * Esta clase habilita y configura el soporte de WebSocket con STOMP en Spring Boot.
 * Define el endpoint de conexión, permite configurar CORS, habilitar SockJS y registra
 * un broker simple de mensajes en memoria.
 * </p>
 *
 * <p>
 * La configuración se basa en propiedades externas (WebSocketProp) para no hardcodear valores
 * y permitir que cambien según el entorno (desarrollo, producción, multi-tenant SaaS).
 * </p>
 *
 * <p>
 * Se agregan interceptores de handshake, como {@link JwtHandshakeInterceptor}, que permiten
 * autenticar usuarios antes de abrir la conexión WebSocket.
 * </p>
 *
 * <p>
 * Uso de prefijos:
 * <ul>
 *     <li>Application Destination Prefix: prefijo para mensajes enviados a controladores Spring (@MessageMapping)</li>
 *     <li>Broker Prefix: prefijo para rutas de suscripción donde los clientes reciben mensajes (@SubscribeMapping o /topic/...)</li>
 * </ul>
 * </p>
 *
 * @author Facundo Palmieri
 * @see org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
 * @see JwtHandshakeInterceptor
 */
@Configuration
@EnableWebSocketMessageBroker // Habilita soporte WebSocket + protocolo STOMP
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketProp props;
    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

    @Autowired
    public WebSocketConfig(WebSocketProp props, JwtHandshakeInterceptor jwtHandshakeInterceptor) {
        this.props = props;
        this.jwtHandshakeInterceptor = jwtHandshakeInterceptor;
    }



    /**
     * Registra el endpoint STOMP para el WebSocket.
     *
     * <p>
     * Este endpoint es la URL a la que los clientes se conectarán para iniciar la sesión
     * WebSocket. Se aplica configuración de CORS, se habilita SockJS si corresponde y
     * se agregan interceptores para autenticación.
     * </p>
     *
     * @param registry Registro de endpoints STOMP
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        // 1) Registra el endpoint del WebSocket (ej: /ws)
        //    Es el punto donde el frontend se conecta por primera vez
        StompWebSocketEndpointRegistration reg = registry
                .addEndpoint(props.getEndpoint())

                // Permitimos CORS según configuración externa
                .setAllowedOrigins("*");

        // 2) Opcionalmente habilita SockJS para cuando el navegador NO soporta WebSocket real
        if (props.isUseSockjs()) {

            // Añade el interceptor JWT también a SockJS
            reg.withSockJS().setInterceptors(jwtHandshakeInterceptor);

        } else {
            // Sin SockJS: agregamos el interceptor directamente
            reg.addInterceptors(jwtHandshakeInterceptor);
        }
    }






    /**
     * Configura el broker de mensajes que se utilizará para enviar y recibir mensajes.
     *
     * <p>
     * Se habilita un broker simple en memoria y se definen prefijos para diferenciar
     * mensajes enviados a controladores de Spring y mensajes de suscripción de clientes.
     * </p>
     *
     * @param registry Registro de broker de mensajes
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        // 3) Definimos a qué prefijo el servidor enviará mensajes
        //    Ej: "/topic" → el profesional se suscribe a /topic/consultas
        registry.enableSimpleBroker(props.getBrokerPrefix());

        // 4) Prefijo de los mensajes que el cliente envía al servidor
        //    Ej: "/app" → los clientes envían mensajes a /app/consultarEstado
        registry.setApplicationDestinationPrefixes(props.getAppPrefix());
    }
}

