package com.odontologiaintegralfm.security.config.filter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.service.interfaces.IMessageService;
import com.odontologiaintegralfm.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collection;

/**
 * Filtro encargado de validar el token JWT en las solicitudes HTTP para asegurar que el usuario esté autenticado.
 * Este filtro intercepta las solicitudes, extrae el token JWT desde el encabezado de autorización, valida su autenticidad,
 * y, si es válido, establece la autenticación en el contexto de seguridad para que el usuario pueda acceder a los recursos protegidos.
 * Si el token está expirado o es inválido, se maneja la excepción correspondiente y se devuelve un mensaje de error adecuado.
 *
 * <p>
 * La clase extiende {@link OncePerRequestFilter} para garantizar que se ejecute una vez por solicitud y se encargue de la validación del token JWT.
 * </p>
 *
 * <p>
 * Los mensajes de error y éxito se obtienen del servicio de mensajes {@link IMessageService}, lo que permite la internacionalización
 * y personalización de los mensajes de respuesta.
 * </p>
 *
 * @see JwtUtils Utiliza este servicio para la validación del token y la extracción de datos del mismo.
 * @see IMessageService Servicio utilizado para obtener los mensajes de error y respuesta personalizados.
 */

@Slf4j
public class JwtTokenValidator extends OncePerRequestFilter {
    private JwtUtils jwtUtils;
    private IMessageService messageService;

    public JwtTokenValidator(JwtUtils jwtUtils, IMessageService messageService) {
        this.jwtUtils = jwtUtils;
        this.messageService = messageService;
    }





    /**
     * Filtra las solicitudes HTTP para autenticar el token JWT presente en el encabezado de autorización.
     * <p>
     * Este filtro extrae el token JWT del encabezado de la solicitud, valida su autenticidad y extrae la información
     * del usuario y los roles asociados. Si el token es válido, se establece la autenticación en el contexto de seguridad
     * para que el usuario pueda acceder a los recursos protegidos.
     * </p>
     *
     * @param request La solicitud HTTP que contiene el encabezado de autorización con el token JWT.
     * @param response La respuesta HTTP que se envía al cliente.
     * @param filterChain La cadena de filtros que permite continuar con el procesamiento de la solicitud después de la autenticación.
     * @throws ServletException Si ocurre un error en el procesamiento del filtro.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    @Override
    //importante: el nonnull debe ser de sringframework, no lombok
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwtToken = request.getHeader(HttpHeaders.AUTHORIZATION);

            if (jwtToken != null) {


                //en el encabezado antes del token viene la palabra bearer (esquema de autenticación)
                //por lo que debemos sacarlo
                jwtToken = jwtToken.substring(7); //son 7 letras + 1 espacio
                DecodedJWT decodedJWT = jwtUtils.validateToken(jwtToken);

                //si el token es válido, le concedemos el acceso
                String username = jwtUtils.extractUsername(decodedJWT);
                //me devuelve claim, necesito pasarlo a String
                String authorities = jwtUtils.getSpecificClaim(decodedJWT, "authorities").asString();

                //Si todo está ok, hay que setearlo en el Context Holder
                //Para eso tengo que convertirlos a GrantedAuthority
                Collection<? extends GrantedAuthority> authoritiesList = AuthorityUtils.commaSeparatedStringToAuthorityList(authorities);

                //Si se valida el token, le damos acceso al usuario en el context holder
                SecurityContext context = SecurityContextHolder.getContext();
                Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, authoritiesList);
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);

            }

            // Continuar con el siguiente filtro
            filterChain.doFilter(request, response);
        }catch (TokenExpiredException ex){
            handleTokenExpiredException(ex, response);
        }catch (JWTVerificationException ex) {
            handleTokenInvalidException(ex,response);
        }
    }



    /**
     * Maneja la excepción de un token JWT expirado.
     * <p>
     * Este método captura la excepción de un token expirado, registra un mensaje de error en el log,
     * y proporciona una respuesta personalizada al cliente informando que el token ha expirado.
     * Se realiza dentro de esta clase ya que el filtro no permite que el manejador global capture la excepción.
     * </p>
     *
     * @param ex La excepción que indica que el token ha expirado.
     * @param response La respuesta HTTP que se enviará al cliente.
     * @throws IOException Si ocurre un error al escribir la respuesta JSON en el cuerpo de la respuesta HTTP.
     */
    private void handleTokenExpiredException(TokenExpiredException ex, HttpServletResponse response) throws IOException {
        // Comprobar si hay una autenticación en el contexto
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = (authentication != null) ? authentication.getName() : "Anónimo";  // Valor por defecto si no se encuentra autenticación

        String logMessage = messageService.getMessage("exception.expiredToken.log", null, LocaleContextHolder.getLocale());
        log.error(logMessage, username);

        // Crear mensaje genérico para el usuario
        String userMessage = messageService.getMessage("exception.expiredToken.user", null, LocaleContextHolder.getLocale());

        // Capturamos la excepción y devolvemos una respuesta personalizada
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");

        // Crear la respuesta con el formato adecuado
        Response<String> customResponse = new Response<>(false, userMessage, null);

        // Convertir el objeto a JSON (usando una librería como Jackson)
        String jsonResponse = new ObjectMapper().writeValueAsString(customResponse);

        // Escribir la respuesta JSON en el cuerpo de la respuesta HTTP
        response.getWriter().write(jsonResponse);
    }



    /**
     * Maneja la excepción cuando un token JWT es inválido.
     * <p>
     * Este método captura la excepción que indica que el token JWT es inválido, registra un mensaje de error en el log,
     * y proporciona una respuesta personalizada al cliente informando que el token es inválido.
     * </p>
     *
     * @param ex La excepción de tipo `JWTVerificationException` que indica que el token JWT es inválido.
     * @param response La respuesta HTTP que se enviará al cliente.
     * @throws IOException Si ocurre un error al escribir la respuesta JSON en el cuerpo de la respuesta HTTP.
     */
    private void handleTokenInvalidException(JWTVerificationException ex, HttpServletResponse response) throws IOException {

        // Comprobar si hay una autenticación en el contexto
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = (authentication != null) ? authentication.getName() : "Unknown User";  // Default value if no authentication is found

        // Cargar el mensaje de error desde properties
        String logMessage = messageService.getMessage("exception.validateToken.log", new Object[]{username}, LocaleContextHolder.getLocale());
        log.error(logMessage, username,ex);

        // Crear mensaje genérico para el usuario
        String userMessage = messageService.getMessage("exception.validateToken.user", null, LocaleContextHolder.getLocale());

        // Capturamos la excepción y devolvemos una respuesta personalizada
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        // Crear la respuesta con el formato adecuado
        Response<String> customResponse = new Response<>(false, userMessage, null);

        // Convertir el objeto a JSON (usando una librería como Jackson)
        String jsonResponse = new ObjectMapper().writeValueAsString(customResponse);

        // Escribir la respuesta JSON en el cuerpo de la respuesta HTTP
        response.getWriter().write(jsonResponse);
    }
}


