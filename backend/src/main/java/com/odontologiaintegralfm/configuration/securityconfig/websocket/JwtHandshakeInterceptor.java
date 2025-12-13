package com.odontologiaintegralfm.configuration.securityconfig.websocket;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.odontologiaintegralfm.configuration.securityconfig.core.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.WebSocketHandler;
import java.util.Map;



/**
 * Interceptor de handshake WebSocket que valida JWT.
 *
 * <p>
 * Esta clase intercepta la apertura de la conexión WebSocket (handshake) y valida
 * la autenticidad del token JWT enviado por el cliente. Si el token es válido,
 * se permite abrir la conexión; si no, se rechaza.
 * </p>
 *
 * <p>
 * Además, extrae información del usuario (username y authorities) y la almacena
 * en los atributos de sesión WebSocket, para que los controladores STOMP puedan
 * identificar al usuario en cada mensaje.
 * </p>
 *
 * <p>
 * No se utiliza la cadena de filtros HTTP de Spring Security, ya que el WebSocket
 * no pasa por ella. La autenticación se realiza exclusivamente en el handshake.
 * </p>
 * @see org.springframework.web.socket.server.HandshakeInterceptor
 * @see JwtUtils
 */
@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtils jwtUtils;

    @Autowired
    public JwtHandshakeInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }


    /**
     * Método que se ejecuta antes de abrir el WebSocket (handshake).
     *
     * <p>
     * Extrae el token JWT de los parámetros de la request o del header
     * "Sec-WebSocket-Protocol", valida el token y almacena el username y las
     * authorities en los atributos de sesión.
     * </p>
     *
     * @param request Request HTTP del handshake
     * @param response Response HTTP del handshake
     * @param wsHandler Handler del WebSocket
     * @param attributes Mapa de atributos de la sesión WebSocket donde se guardan datos del usuario
     * @return true si el token es válido y se permite la conexión, false si se rechaza
     * @throws Exception Si ocurre un error en la validación del token
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        // 1. Obtener JWT desde query param o headers
        String token = null;

        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpReq = servletRequest.getServletRequest();
            token = httpReq.getParameter("token");

            if (token == null) {
                token = httpReq.getHeader("Sec-WebSocket-Protocol");
            }
        }

        if (token == null) {
            return false; // handshake rechazado
        }

        try {
            // 2. Validar token
            DecodedJWT decoded = jwtUtils.validateToken(token);

            // 3. Guardar info del usuario para que STOMP la lea
            attributes.put("username", jwtUtils.extractUsername(decoded));
            attributes.put("authorities",
                    jwtUtils.getSpecificClaim(decoded, "authorities").asString());

            return true;

        } catch (Exception e) {
            return false; // rechazar handshake
        }
    }





    /**
     * Método que se ejecuta después de abrir el WebSocket (handshake).
     *
     * <p>
     * Se puede usar para logging, métricas o limpieza de recursos. En esta implementación,
     * no se requiere ninguna acción posterior al handshake.
     * </p>
     *
     * @param request Request HTTP del handshake
     * @param response Response HTTP del handshake
     * @param wsHandler Handler del WebSocket
     * @param exception Excepción que ocurrió durante el handshake, si la hay
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
    }
}