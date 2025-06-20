package com.odontologiaintegralfm.exception;

import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.enums.LogLevel;
import com.odontologiaintegralfm.service.interfaces.IMessageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import java.util.HashMap;
import java.util.Map;
/**
 * Manejador global de excepciones para capturar y gestionar diferentes tipos de excepciones lanzadas en la aplicación.
 * <p>
 * Esta clase captura excepciones de diferentes tipos, las registra en los logs y proporciona respuestas personalizadas
 * para el usuario con códigos de estado HTTP apropiados. Se utiliza la anotación {@link ControllerAdvice} para manejar
 * las excepciones globalmente en toda la aplicación.
 * </p>
 *
 * Las excepciones manejadas incluyen:
 * - {@link AppException}: Maneja excepciones personalizadas.
 * - {@link MethodArgumentNotValidException}: Excepciones de validación de parámetros en objetos de transferencia de datos (DTOs).
 * - {@link DataBaseException}: Excepciones relacionadas con la base de datos.
 * - {@link NoHandlerFoundException}: Excepciones relacionadas con la falta de un manejador adecuado para una solicitud.
 * - {@link NoResourceFoundException}: Excepciones relacionadas con recursos solicitados que no existen en el sistema.
 * - {@link AccessDeniedException}: Excepciones relacionadas con el acceso denegado debido a permisos insuficientes o autenticación fallida.
 * - {@link ExceptionHandler}: Maneja cualquier excepción no capturada, proporcionando una respuesta genérica para errores inesperados.
 */

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private IMessageService messageService;

    /**
     * Maneja excepciones personalizadas del tipo {@link AppException}.
     * <p>
     * Extrae los mensajes para el usuario desde el servicio de mensajes, construye el mensaje de log
     * si corresponde según el nivel indicado en la excepción, y devuelve una respuesta estándar con el
     * mensaje para el cliente.
     * </p>
     *
     * @param e la excepción {@link AppException} capturada.
     * @return una {@link ResponseEntity} con un objeto {@link Response} que contiene el mensaje para el usuario
     *         y el estado HTTP definido en la excepción.
     */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<Response<Void>> handleAppException(AppException e) {

        //Mensaje para el usuario.
        String userMessage = messageService.getMessage(e.getUserMessageKey(), e.getUserArgs(), LocaleContextHolder.getLocale());

        //Verifica si corresponde loguear.
        if (e.getLogLevel() != LogLevel.NONE) {


            //Construye mensaje para el log
            String logMessage = messageService.getMessage(e.getLogMessageKey(),e.getLogArgs(), LocaleContextHolder.getLocale());

            // Loguea de acurdo al nivel.
            switch (e.getLogLevel()) {
                case INFO -> log.info(logMessage, e);
                case WARN -> log.warn(logMessage, e);
                case ERROR -> log.error(logMessage, e);
            }
        }
        //Construir respuesta y enviar.
        Response<Void> response = new Response<>(false, userMessage, null);

        return new ResponseEntity<>(response, e.getStatus());
    }


    /**
     * Maneja las excepciones de tipo {@link MethodArgumentNotValidException}, que se lanzan cuando hay violaciones de validación
     * en los objetos de transferencia de datos (DTOs), como cuando los datos no cumplen con las restricciones establecidas en
     * las anotaciones de validación (por ejemplo, `@NotNull`, `@Size`, etc.).
     * <p>
     * Este manejador de excepciones captura los errores de validación de los campos de los DTOs, los traduce a mensajes
     * legibles para el usuario y los devuelve en una respuesta con el estado HTTP {@code 400 Bad Request}.
     * Además, registra los errores de validación en los logs para realizar un seguimiento.
     * </p>
     *
     * @param ex La excepción {@link MethodArgumentNotValidException} que contiene los detalles de las violaciones de validación.
     * @return Una respuesta con un mapa de los errores de validación y un código de estado HTTP {@code 400 Bad Request}.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)  // Esta excepción se lanza cuando hay una violación de validación de un objeto (por ejemplo, DTO)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {

        // Crear un mapa para almacenar los errores de validación
        Map<String, String> errors = new HashMap<>();

        // Iteramos sobre los errores de cada campo que falló en la validación
        ex.getBindingResult().getFieldErrors().forEach(error ->{
            String errorMessage = messageService.getMessage(error.getDefaultMessage(), null, LocaleContextHolder.getLocale());   // Guardamos el nombre del campo (error.getField()) y el mensaje de error correspondiente (error.getDefaultMessage()) en el mapa
            errors.put(error.getField(), errorMessage);

            //Obtiene el usuario autenticado.
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = (authentication != null) ? authentication.getName() : "Anónimo";

            //Guarda el log
            String logMessage = messageService.getMessage("exception.validation.log",new Object[]{error.getField(),errorMessage,username}, LocaleContextHolder.getLocale());
            log.error(logMessage);
        });

        Response<Map<String, String>> response = new Response<>(
                false,
                "Error de validación en DTO",
                errors
        );

        // Devolvemos una respuesta con un código de estado HTTP 400 (Bad Request) y el mapa de errores como cuerpo de la respuesta
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);  // Enviamos los errores de validación como cuerpo de la respuesta
    }



    /**
     * Maneja las excepciones de tipo {@link DataBaseException}, que se lanzan cuando ocurre un error relacionado con la base de datos,
     * como fallos en las operaciones de persistencia o cuando se intenta acceder a una entidad no válida.
     * <p>
     * Este manejador captura los detalles de la excepción, los traduce a un mensaje amigable para el usuario y los registra en los logs
     * para realizar un seguimiento. Luego, responde al cliente con un mensaje de error y un estado HTTP {@code 500 Internal Server Error}.
     * </p>
     *
     * @param ex La excepción {@link DataBaseException} que contiene los detalles del error en la base de datos.
     * @return Una respuesta con un mensaje genérico de error para el usuario y un código de estado HTTP {@code 500 Internal Server Error}.
     */
    @ExceptionHandler(DataBaseException.class)
    public ResponseEntity<Response<Void>> handleDataBaseException(DataBaseException ex) {

        //Se construye en el mensaje para el usuario.
        String userMessage = messageService.getMessage("exception.database.user", null, LocaleContextHolder.getLocale());

        //Se construye en el mensaje para log.
        String logMessage = messageService.getMessage(
                "exception.database.log",
                new Object[]{ ex.getClase(),  ex.getEntityId(), ex.getEntityName(), ex.getMethod(), ex.getRootCause(), userMessage},
                LocaleContextHolder.getLocale()
                );

        log.error(logMessage);

        // Respuesta a usuario.
        Response<Void> response = new Response<>(false, userMessage, null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }



    /**
     * Maneja las excepciones {@link NoHandlerFoundException} y {@link NoResourceFoundException}, que se lanzan cuando
     * no se puede encontrar un manejador adecuado para una solicitud o cuando un recurso solicitado no existe.
     * <p>
     * Este manejador captura las excepciones lanzadas en casos donde no se encuentra el recurso o el manejador de la solicitud,
     * registra el error y devuelve un mensaje amigable para el usuario, con un estado HTTP {@code 404 Not Found}.
     * </p>
     *
     * @param ex La excepción {@link Exception} que representa un error de recurso no encontrado o un manejador no encontrado.
     * @return Una respuesta con un mensaje específico para el usuario y un código de estado HTTP {@code 404 Not Found}.
     */
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<Response<Void>> handleNotFound(Exception ex) {
        String messageUser = messageService.getMessage("exception.notFound", null, LocaleContextHolder.getLocale());
        Response<Void> response = new Response<>(false, messageUser, null);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }


    /**
     * Maneja las excepciones de acceso denegado en la aplicación.
     * <p>
     * Este método intercepta las excepciones {@link AccessDeniedException} generadas cuando un usuario intenta acceder
     * a un recurso para el cual no tiene permisos suficientes. Se distingue entre dos casos:
     * <ul>
     *     <li>Si el usuario no está autenticado (es un usuario anónimo), se devuelve un código HTTP 401 (Unauthorized).</li>
     *     <li>Si el usuario está autenticado pero no tiene los permisos adecuados, se devuelve un código HTTP 403 (Forbidden).</li>
     * </ul>
     * </p>
     *
     * @param ex      La excepción de acceso denegado capturada.
     * @param request La solicitud HTTP que generó la excepción.
     * @return Una {@link ResponseEntity} con un objeto {@link Response} que contiene el mensaje de error y el código HTTP correspondiente.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Response<Void>> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {

        // Obtener información del usuario autenticado, si existe
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Obtener la URL del endpoint al que se intentó acceder
        String requestedUrl = request.getRequestURI();

        if(username.equals("anonymousUser")) {
            // Obtener el mensaje desde el archivo de propiedades y asignar datos
            String logMessage = messageService.getMessage("exception.authenticationRequired.log", new Object[]{username, requestedUrl, ex.getMessage()}, LocaleContextHolder.getLocale());

            // Loguear la excepción para detalles de diagnóstico
            log.error(logMessage, ex);

            // Mensaje para el usuario final
            String messageUser = messageService.getMessage("exception.authenticationRequired.user", null, LocaleContextHolder.getLocale());
            Response<Void> response = new Response<>(false, messageUser, null);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }else{
            // Obtener el mensaje desde el archivo de propiedades y asignar datos
            String logMessage = messageService.getMessage("exception.accessDenied.log", new Object[]{username, requestedUrl, ex.getMessage()}, LocaleContextHolder.getLocale());

            // Loguear la excepción para detalles de diagnóstico
            log.error(logMessage, ex);

            // Mensaje para el usuario final
            String messageUser = messageService.getMessage("exception.accessDenied.user", null, LocaleContextHolder.getLocale());
            Response<Void> response = new Response<>(false, messageUser, null);
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }
    }



    /**
     * Maneja cualquier excepción no capturada, proporcionando una respuesta genérica para errores inesperados.
     * <p>
     * Este manejador captura excepciones generales que no sean específicas, registra el error en el log con los detalles
     * relevantes para el diagnóstico y devuelve un mensaje de error genérico al usuario, con un código de estado HTTP
     * {@code 500 Internal Server Error}.
     * </p>
     *
     * @param e La excepción {@link Exception} que representa un error inesperado que ocurrió en el sistema.
     * @return Una respuesta con un mensaje genérico para el usuario y un código de estado HTTP {@code 500 Internal Server Error}.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Void>> handleGeneralException(Exception e) {
        // Loguear la excepción para detalles de diagnóstico
        log.error("Error inesperado: " + e.getMessage(), e);

        // Respuesta genérica para cualquier excepción no capturada
        String messageUser = messageService.getMessage("exception.generic", null, LocaleContextHolder.getLocale());
        Response<Void> response = new Response<>(false, messageUser, null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
