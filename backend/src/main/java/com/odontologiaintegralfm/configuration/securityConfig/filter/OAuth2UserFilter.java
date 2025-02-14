package com.odontologiaintegralfm.configuration.securityConfig.filter;

/**
 * Filtro para la autenticación y validación de usuarios OAuth2, y generación de tokens JWT.
 *
 * <p>
 * Este filtro se encarga de verificar si un usuario está autenticado mediante OAuth2, y en caso afirmativo, valida
 * que la cuenta esté habilitada y no esté bloqueada. Si el usuario cumple con estos requisitos, se genera un token JWT
 * y se agrega al encabezado "Authorization" de la respuesta HTTP. Si la cuenta está deshabilitada o bloqueada, se maneja
 * la situación y se envía una respuesta de error al cliente.
 * </p>
 *
 * <p>
 * El filtro también obtiene la información del usuario, como el correo electrónico, desde la autenticación OAuth2 y la valida
 * consultando la base de datos para asegurarse de que el usuario esté habilitado y que su cuenta no esté bloqueada.
 * </p>
 *
 * <p>
 * Este filtro se ejecuta una vez por solicitud HTTP, asegurándose de que el usuario esté correctamente autenticado y validado.
 * </p>
 */
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.model.UserSec;
import com.odontologiaintegralfm.repository.IUserRepository;
import com.odontologiaintegralfm.service.interfaces.IMessageService;
import com.odontologiaintegralfm.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Optional;
@Slf4j
public class OAuth2UserFilter extends OncePerRequestFilter {

        private JwtUtils jwtUtils;
        private IMessageService messageService;
        private IUserRepository userRepository;

        public OAuth2UserFilter(JwtUtils jwtUtils,IUserRepository userRepository, IMessageService messageService) {
            this.jwtUtils = jwtUtils;
            this.userRepository = userRepository;
            this.messageService = messageService;
        }

    /**
     * Filtra la solicitud HTTP para verificar la autenticación del usuario OAuth2 y generar un token JWT si es necesario.
     *
     * <p>
     * Este método comprueba si el usuario está autenticado mediante OAuth2. Si el usuario está autenticado y su cuenta está habilitada
     * y no bloqueada, se genera un token JWT utilizando la autenticación proporcionada y se agrega al encabezado "Authorization" de la respuesta.
     * Si la cuenta está deshabilitada o bloqueada, maneja esas situaciones adecuadamente.
     * </p>
     *
     * <p>
     * El filtro también obtiene la información del usuario (como el correo electrónico) desde la autenticación de OAuth2 y la valida
     * consultando la base de datos para asegurarse de que el usuario esté habilitado y no tenga la cuenta bloqueada.
     * </p>
     *
     * @param request La solicitud HTTP que contiene los detalles de la petición.
     * @param response La respuesta HTTP que se enviará al cliente, que incluirá el token JWT en el encabezado "Authorization" si el usuario está autenticado y la cuenta es válida.
     * @param filterChain La cadena de filtros que permite pasar la solicitud al siguiente filtro en el proceso de autenticación.
     * @throws ServletException Si ocurre un error general durante el procesamiento del filtro.
     * @throws IOException Si ocurre un error de entrada/salida durante el procesamiento del filtro.
     * @throws DataBaseException Si ocurre un error al interactuar con la base de datos para verificar el estado del usuario.
     */

    @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException, IOException {
            try{

                // Obtiene la autenticación actual del contexto de seguridad.
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                // Verifica si la autenticación no es nula y el principal es una instancia de DefaultOAuth2User.
                if (authentication != null && authentication.getPrincipal() instanceof DefaultOAuth2User) {
                    DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();

                    // Obtiene el email del usuario autenticado.
                    String email = oAuth2User.getAttribute("email");

                    // Recupera datos del usuario.
                    Optional<UserSec> userOptional = userRepository.findUserEntityByUsername(email);
                    UserSec user = userOptional.orElse(null);

                    //Verifica si existe en la BD o si la cuenta está inactiva.
                    if (user == null || !user.isEnabled()) {
                        // Si el usuario no existe o no está habilitado
                        handleEnableAccount(user, response);
                        return;
                    }

                    //Verifica si la cuenta está bloqueada
                    if (!user.isAccountNotLocked()) {
                        handleBlockAccount(user, response);
                        return;
                    }

                    // Si el usuario está registrado, se genera un JWT usando el método createToken.
                    String jwt = jwtUtils.createToken(authentication);

                    // Se incluye el token en el encabezado de la respuesta con el formato "Bearer [token]".
                     response.addHeader("Authorization", "Bearer " + jwt);
                }

                // Pasa la solicitud al siguiente filtro en la cadena de filtros.
                filterChain.doFilter(request, response);
            }catch (DataBaseException e){
                throw new DataBaseException(e,"OAuth2UserFilter",0L , "", "Método doFilterInternal");

            }

        }

    /**
     * Maneja la situación en la que una cuenta de usuario está deshabilitada o no existe.
     *
     * <p>
     * Este método verifica si el usuario está habilitado o si no se encuentra en la base de datos. Si el usuario no está habilitado o no existe,
     * se registra un mensaje de error en los logs y se envía una respuesta HTTP con un mensaje de error al cliente indicando que el usuario no fue encontrado o está deshabilitado.
     * Se realiza en esta clase porque el filtro no permite que el manejador global capture la excepción.
     * </p>
     *
     * @param user El objeto que representa al usuario que se va a verificar.
     * @param response La respuesta HTTP que se enviará al cliente con un mensaje de error.
     * @return {@code true} si el usuario no existe o está deshabilitado, en cuyo caso se maneja el error y se envía una respuesta;
     *         {@code false} si el usuario está habilitado y no se requiere manejo adicional.
     * @throws IOException Si ocurre un error al escribir la respuesta JSON en el cuerpo de la respuesta HTTP.
     */

    private boolean handleEnableAccount(UserSec user, HttpServletResponse response) throws IOException {
            if(user == null || !user.isEnabled()) {
                String logMessage = messageService.getMessage("exception.usernameNotFound.log", new Object[]{user != null ? user.getUsername() : "N/A"}, LocaleContextHolder.getLocale());
                log.error(logMessage);

                // Crear mensaje genérico para el usuario
                String messageUser = messageService.getMessage("exception.usernameNotFound.user", null, LocaleContextHolder.getLocale());

                // Capturamos la excepción y devolvemos una respuesta personalizada
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType("application/json");

                // Crear la respuesta con el formato adecuado
                Response<String> customResponse = new Response<>(false, messageUser, null);

                // Convertir el objeto a JSON (usando una librería como Jackson)
                String jsonResponse = new ObjectMapper().writeValueAsString(customResponse);

                // Escribir la respuesta JSON en el cuerpo de la respuesta HTTP
                response.getWriter().write(jsonResponse);
                return true;
            }
            return false;

        }


    /**
     * Maneja la situación en la que una cuenta de usuario está bloqueada.
     *
     * <p>
     * Este método verifica si el usuario está bloqueado . Si es así, se registra un mensaje de error en los logs
     * y se envía una respuesta HTTP con un mensaje de error al cliente indicando que el usuario se encuentra bloqueado.
     * Se realiza en esta clase porque el filtro no permite que el manejador global capture la excepción.
     * </p>
     *
     * @param user El objeto que representa al usuario que se va a verificar.
     * @param response La respuesta HTTP que se enviará al cliente con un mensaje de error.
     * @return {@code true} si el usuario está bloqueado, en cuyo caso se maneja el error y se envía una respuesta;
     *         {@code false} si el usuario está habilitado y no se requiere manejo adicional.
     * @throws IOException Si ocurre un error al escribir la respuesta JSON en el cuerpo de la respuesta HTTP.
     */
    private boolean handleBlockAccount(UserSec user, HttpServletResponse response) throws IOException {

        if(!user.isAccountNotLocked()) {
            String logMessage = messageService.getMessage("exception.blockAccount.log",new Object[]{user.getId(),user.getUsername()},LocaleContextHolder.getLocale());
            log.error(logMessage);

            // Crear mensaje genérico para el usuario
            String messageUser = messageService.getMessage("exception.blockAccount.user", null, LocaleContextHolder.getLocale());

            // Capturamos la excepción y devolvemos una respuesta personalizada
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");

            // Crear la respuesta con el formato adecuado
            Response<String> customResponse = new Response<>(false, messageUser, null);

            // Convertir el objeto a JSON (usando una librería como Jackson)
            String jsonResponse = new ObjectMapper().writeValueAsString(customResponse);

            // Escribir la respuesta JSON en el cuerpo de la respuesta HTTP
            response.getWriter().write(jsonResponse);
            return true;
        }
        return false;

    }
}
