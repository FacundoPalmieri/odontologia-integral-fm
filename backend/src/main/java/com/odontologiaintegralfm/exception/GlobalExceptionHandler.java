package com.odontologiaintegralfm.exception;

import com.odontologiaintegralfm.dto.Response;
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
import org.springframework.web.bind.annotation.ResponseStatus;
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
 * - {@link RefreshTokenException}: Excepción personalizada para manejar errores de Refresh Token.
 * - {@link RefreshTokenConfigNotFoundException}: Excepciones relacionadas con la configuración de refresh token.
 * - {@link TokenConfigNotFoundException}: Excepciones relacionadas con la configuración del token.
 * - {@link TokenInvalidException}: Excepciones relacionadas con tokens de autenticación inválidos.
 * - {@link BlockAccountException}: Excepciones cuando una cuenta de usuario está bloqueada.
 * - {@link ResourceNotFoundException}: Excepciones cuando un recurso no se encuentra.
 * - {@link UserNameExistingException}: Excepciones cuando el nombre de usuario ya está en uso.
 * - {@link UserNameNotFoundException}: Excepciones cuando no se encuentra un nombre de usuario.
 * - {@link UserNotFoundException}: Excepciones cuando no se encuentra un usuario por su ID.
 * - {@link MethodArgumentNotValidException}: Excepciones de validación de parámetros en objetos de transferencia de datos (DTOs).
 * - {@link DataBaseException}: Excepciones relacionadas con la base de datos.
 * - {@link RoleNotFoundUserCreationException}: Excepciones relacionadas con roles no encontrados al crear un usuario o asignar un rol.
 * - {@link RoleNotFoundException}: Excepciones relacionadas con roles no encontrados en la base de datos o en el sistema.
 * - {@link RoleExistingException}: Excepciones relacionadas con roles que ya existen en el sistema o en la base de datos al intentar crear o actualizar un rol.
 * - {@link PermissionNotFoundRoleCreationException}: Excepciones relacionadas con permisos faltantes al crear un rol en el sistema.
 * - {@link PermissionNotFoundException}: Excepciones relacionadas con permisos no encontrados en el sistema.
 * - {@link NoHandlerFoundException}: Excepciones relacionadas con la falta de un manejador adecuado para una solicitud.
 * - {@link NoResourceFoundException}: Excepciones relacionadas con recursos solicitados que no existen en el sistema.
 * - {@link AccessDeniedException}: Excepciones relacionadas con el acceso denegado debido a permisos insuficientes o autenticación fallida.
 * - {@link CredentialsException}: Excepciones relacionadas con credenciales incorrectas durante el proceso de autenticación.
 * - {@link PasswordMismatchException}: Excepciones relacionadas con el incumplimiento de la coincidencia de contraseñas durante operaciones de cambio de contraseña o actualización de usuario.
 * - {@link UserUpdateSelfUpdateException}: Excepciones relacionadas con un usuario que intenta actualizar sus propios datos, lo cual no está permitido.
 * - {@link UserSaveNotDevRoleException}: Excepciones que ocurren cuando se intenta asignar un rol de desarrollador.
 * - {@link UserUpdateNotDevRoleException}: Excepciones relacionadas con un intento de actualización de un usuario que posee un rol no permitido o está intentando asignarse un rol de desarrollador.
 * - {@link UserUpdateException}: Excepciones relacionadas con errores durante el proceso de actualización de un usuario.
 * - {@link MessageNotFoundException}: Excepciones que ocurren cuando no se encuentra un mensaje específico durante una operación de actualización.
 * - {@link ExceptionHandler}: Maneja cualquier excepción no capturada, proporcionando una respuesta genérica para errores inesperados.
 */

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private IMessageService messageService;


    /**
     *Maneja las excepciones del tipo {@link RefreshTokenException} que ocurre cuando un refresh token no es encontrado, es inválido o está expirado.
     *<p>
     * Este manejador de excepciones captura los casos registrando la excepeción para el análisis y proporcionando una respuesta adecuada al cliente.
     *</p>
     */

    @ExceptionHandler({RefreshTokenException.class})
    public ResponseEntity<Response<Void>> handleRefreshTokenException(RefreshTokenException e) {

        //Obtiene la clave enviada desde UserDetailServiceImp para luego loguearla.
        String message = messageService.getMessage(e.getMessage(), null,LocaleContextHolder.getLocale());

        //Cargar mensaje para el log.
        String logMessage = messageService.getMessage("exception.refreshToken.log", new Object[]{e.getEntityType(), e.getOperation(),e.getId(),message}, LocaleContextHolder.getLocale());
        log.error(logMessage, e);

        //Construir respuesta y enviar.
        Response<Void> response = new Response<>(false, logMessage, null);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);

    }

    /**
     *Maneja las excepciones del tipo {@link RefreshTokenConfigNotFoundException} que ocurre cuando no se encuentra el tiempo de expiración del token.
     *<p>
     * Este manejador de excepciones captura los casos en los que no se encuentra en la base de datos el tiempo de expiración del token, registrando la excepeción
     * para el análisis y proporcionando una respuesta adecuada al cliente.
     *</p>
     */
    @ExceptionHandler({RefreshTokenConfigNotFoundException.class})
    public ResponseEntity<Response<Void>> handleRefreshTokenConfigNotFoundException(RefreshTokenConfigNotFoundException e) {
        //Cargar mensaje para el log.
        String logMessage = messageService.getMessage("exception.refreshTokenConfigNotFoundException.log",new Object[]{e.getEntityType(), e.getOperation()}, LocaleContextHolder.getLocale());

        log.error(logMessage, e);

        //Cargar mensaje para el usuario
        String userMessage = messageService.getMessage("exception.refreshTokenConfigNotFoundException.user", null, LocaleContextHolder.getLocale());

        //Construir respuesta y enviar.
        Response <Void> response = new Response<>(false,userMessage,null);

        // Retornar la respuesta con el código de estado 401 (Unauthorized).
        return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
    }






    /**
     * Meneja las excepciones del tipo {@link TokenConfigNotFoundException} que ocurre cuando no se encuentra el tiempo de expiración del token.
     * <p>
     *  Este manejador de excepciones captura los casos en los que no se encuentra en la base de datos el tiempo de expiración del token, registrando la excepeción
     *  para el análisis y proporcionando una respuesta adecuada al cliente.
     * </p>
     */
    @ExceptionHandler({TokenConfigNotFoundException.class})
    public ResponseEntity<Response<Void>> handleTokenConfigNotFoundException(TokenConfigNotFoundException e) {
        //Cargar mensaje para el log.
        String logMessage = messageService.getMessage("exception.tokenConfigNotFoundException.log",new Object[]{e.getEntityType(), e.getOperation()}, LocaleContextHolder.getLocale());

        log.error(logMessage, e);

        //Cargar mensaje para el usuario
        String userMessage = messageService.getMessage("exception.tokenConfigNotFoundException.user", null, LocaleContextHolder.getLocale());

        //Construir respuesta y enviar.
        Response<Void> response = new Response<>(false,userMessage,null);

        // Retornar la respuesta con el código de estado 401 (Unauthorized).
        return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
    }



    /**
     * Maneja las excepciones de tipo {@link TokenInvalidException} que ocurren cuando un token es inválido.
     * <p>
     * Este manejador de excepciones captura los casos en los que un token de autenticación no es válido,
     * registrando la excepción para el análisis y proporcionando una respuesta apropiada para el usuario.
     * </p>
     *
     * @param ex La excepción {@link TokenInvalidException} que contiene los detalles sobre el token inválido.
     * @param request La solicitud HTTP que contiene la información de la petición, incluyendo la IP del usuario.
     * @return Una respuesta con un mensaje de error para el usuario, indicando que el token es inválido.
     */
    @ExceptionHandler({TokenInvalidException.class})
    public ResponseEntity<Response<Void>> handleTokenInvalidException(TokenInvalidException ex, HttpServletRequest request) {
        //Obtener IP
        String ip = request.getRemoteAddr();

        //Cargar mensaje para el log.
        String logMessage = messageService.getMessage("exception.validateToken.log",new Object[]{ex.getUsername(),ip}, LocaleContextHolder.getLocale());

        log.error(logMessage, ex);

        //Cargar mensaje para el usuario.
        String userMessage = messageService.getMessage("exception.validateToken.user", null, LocaleContextHolder.getLocale());

        //Construir respuesta y enviar.
        Response<Void> response = new Response<>(false,userMessage,null);

        // Retornar la respuesta con el código de estado 401 (Unauthorized).
        return new ResponseEntity<>(response,HttpStatus.UNAUTHORIZED);
    }



    /**
     * Maneja las excepciones de tipo {@link BlockAccountException} que ocurren cuando una cuenta de usuario está bloqueada.
     * <p>
     * Este manejador de excepciones captura los casos en los que una cuenta de usuario es bloqueada,
     * registrando la excepción para el análisis y proporcionando una respuesta adecuada para el usuario.
     * </p>
     *
     * @param ex La excepción {@link BlockAccountException} que contiene los detalles sobre la cuenta bloqueada.
     * @return Una respuesta con un mensaje de error para el usuario, indicando que la cuenta está bloqueada.
     */
    @ExceptionHandler(BlockAccountException.class)
    public ResponseEntity<Response<String>> handleBlockAccountException(BlockAccountException ex) {

        // Cargar el mensaje de error para el log.
        String logMessege = messageService.getMessage(
                "exception.blockAccount.log",
                new Object[]{ex.getId(),ex.getUsername()},
                LocaleContextHolder.getLocale()
        );

        log.error(logMessege,ex);

        //Cargar el mensaje de error para el usuario.
        String userMessege = messageService.getMessage(
                "exception.blockAccount.user",
                null,
                LocaleContextHolder.getLocale()
        );

        //Construir respuesta y enviar.
        Response<String> response = new Response<>(false,userMessege,ex.getUsername());

        // Retornar la respuesta con el código de estado 403 (Forbidden), ya que el acceso está prohibido debido a la cuenta bloqueada.
        return new ResponseEntity<>(response,HttpStatus.FORBIDDEN);

    }


    /**
     * Maneja las excepciones de tipo {@link ResourceNotFoundException} que ocurren cuando un recurso no se encuentra.
     * <p>
     * Este manejador de excepciones captura los casos en los que un recurso específico no es encontrado en la base de datos,
     * proporcionando una respuesta adecuada para el usuario y registrando el error para su análisis.
     * </p>
     *
     * @param ex La excepción {@link ResourceNotFoundException} que contiene el ID del recurso no encontrado.
     * @return Una respuesta con un mensaje de error para el usuario, indicando que el recurso no fue encontrado.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Response<Void>> handleResourceNotFoundException(ResourceNotFoundException ex) {

        // Cargar el mensaje de error desde Base de datos
        String userMessage = messageService.getMessage(
                "exception.resourceNotFoundException.user", // La clave de la propiedad
                new Object[]{ex.getId()}, // Pasamos el username como parámetro
                LocaleContextHolder.getLocale() // Usamos el locale para la localización
        );

        // Crear la respuesta con el mensaje personalizado
        Response<Void> response = new Response<>(false, userMessage, null);

        // Retornar la respuesta con el código de estado 404 (Not Found), indicando que el recurso no fue encontrado.
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }


    /**
     * Maneja las excepciones de tipo {@link UserNameExistingException} que ocurren cuando un nombre de usuario ya está registrado.
     * <p>
     * Este manejador de excepciones captura los casos en los que se intenta registrar o usar un nombre de usuario
     * que ya existe en el sistema. Proporciona una respuesta con un mensaje adecuado para el usuario y registra el error
     * para su posterior análisis.
     * </p>
     *
     * @param ex La excepción {@link UserNameExistingException} que contiene el nombre de usuario, tipo de entidad y operación.
     * @return Una respuesta con un mensaje de error para el usuario, indicando que el nombre de usuario ya está en uso.
     */
    @ExceptionHandler(UserNameExistingException.class)
    public ResponseEntity<Response<Void>> handleUsernameExistingException(UserNameExistingException ex) {
        //Construir mensaje para el log
        String logMessage = messageService.getMessage("exception.usernameExisting.log", new Object[]{ex.getEntityType(), ex.getOperation(), ex.getUsername()}, LocaleContextHolder.getLocale());
        log.error(logMessage);

        // Crear la respuesta con el mensaje para el usuario y un estado HTTP 409 (Conflict).
        String userMessage = messageService.getMessage("exception.usernameExisting.user", new Object[]{ex.getUsername()}, LocaleContextHolder.getLocale());
        Response<Void> response = new Response<>(false, userMessage, null);
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }





    /**
     * Maneja las excepciones de tipo {@link UserNameNotFoundException} que ocurren cuando no se encuentra un nombre de usuario al intentar autenticar.
     * <p>
     * Este manejador de excepciones se activa cuando el sistema no encuentra el nombre de usuario proporcionado durante
     * el proceso de autenticación. El método registra el error para su posterior análisis y proporciona un mensaje genérico
     * para el usuario indicando que el nombre de usuario no fue encontrado.
     * </p>
     *
     * @param ex La excepción {@link UserNameNotFoundException} que contiene el nombre de usuario no encontrado.
     * @param request La solicitud HTTP que se realiza, útil para obtener detalles adicionales como la IP.
     * @return Una respuesta con un mensaje de error para el usuario, indicando que el nombre de usuario no fue encontrado.
     */
    @ExceptionHandler(UserNameNotFoundException.class)
    public ResponseEntity<Response<Void>> handleUsernameNotFoundException(UserNameNotFoundException ex, HttpServletRequest request) {

        // Cargar el mensaje de error desde BD
        String logMessage = messageService.getMessage("exception.usernameNotFound.log", new Object[]{ex.getUsername()}, LocaleContextHolder.getLocale());
        log.error(logMessage);


        // Crear mensaje genérico para el usuario
        String userMessage = messageService.getMessage(
                "exception.usernameNotFound.user",
                null,
                LocaleContextHolder.getLocale()
        );

        // Crear la respuesta con el mensaje personalizado
        Response<Void> response = new Response<>(false, userMessage, null);

        // Enviar la respuesta con el código de estado 401.
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);

    }


    /**
     * Maneja las excepciones de tipo {@link UserNotFoundException} que ocurren cuando no se encuentra un usuario en la base de datos
     * durante un intento de búsqueda por ID (por ejemplo, con un método como {@code findById}).
     * <p>
     * Este manejador de excepciones se activa cuando el sistema no puede encontrar el usuario especificado en la base de datos
     * por su ID. El método registra el error para su posterior análisis y proporciona un mensaje genérico para el usuario
     * indicando que el recurso no fue encontrado.
     * </p>
     *
     * @param ex La excepción {@link UserNotFoundException} que contiene la información sobre el usuario no encontrado.
     * @return Una respuesta con un mensaje de error para el usuario, indicando que el usuario no fue encontrado.
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Response<Void>> handleUserNotFoundException(UserNotFoundException ex) {

        //Construye mensaje para el log
        String logMessage = messageService.getMessage("userService.findById.error.log", new Object[]{ex.getEntityType(), ex.getOperation(), ex.getId()}, LocaleContextHolder.getLocale());
        log.error(logMessage);

        //Cargar mensaje para usuario.
        String userMessage = messageService.getMessage("userService.findById.error.user", null, LocaleContextHolder.getLocale());
        Response<Void> response = new Response<>(false, userMessage, null);

        // Devolver la respuesta con un código de estado HTTP NOT_FOUND (404).
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);

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

            //Se guarda Log
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = (authentication != null) ? authentication.getName() : "Anónimo";

            String logMessage = messageService.getMessage("exception.validation.log",new Object[]{error.getField(),errorMessage,username}, LocaleContextHolder.getLocale());
            log.error(logMessage);
        });


        // Devolvemos una respuesta con un código de estado HTTP 400 (Bad Request) y el mapa de errores como cuerpo de la respuesta
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);  // Enviamos los errores de validación como cuerpo de la respuesta
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
                new Object[]{ ex.getEntityType(),  ex.getEntityId(), ex.getEntityName(), ex.getOperation(), ex.getRootCause(), userMessage},
                LocaleContextHolder.getLocale()
                );

        log.error(logMessage);

        // Respuesta a usuario.
        Response<Void> response = new Response<>(false, userMessage, null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }



    /**
     * Maneja las excepciones de tipo {@link RoleNotFoundUserCreationException}, que se lanzan cuando no se encuentra un rol
     * especificado al crear un usuario o asignar un rol.
     * <p>
     * Este manejador captura los detalles de la excepción, los traduce a un mensaje amigable para el usuario y los registra en los logs
     * para realizar un seguimiento. Luego, responde al cliente con un mensaje de error y un estado HTTP {@code 400 Bad Request}.
     * </p>
     *
     * @param ex La excepción {@link RoleNotFoundUserCreationException} que contiene los detalles sobre el error relacionado con la creación de un usuario y el rol.
     * @return Una respuesta con un mensaje genérico de error para el usuario y un código de estado HTTP {@code 400 Bad Request}.
     */
    @ExceptionHandler({RoleNotFoundUserCreationException.class})
    public ResponseEntity<Response<Void>> handleRoleNotFoundUserCreationException(RoleNotFoundUserCreationException ex) {

        //Se construye mensaje para el log
        String messageLog = messageService.getMessage("exception.roleNotFoundUserCreationException.log", new Object[]{ex.getId(), ex.getRole()}, LocaleContextHolder.getLocale());
        log.error(messageLog);

        //Se construye mensaje para el usuario.
        String messageUser = messageService.getMessage("exception.roleNotFoundUserCreationException.user", null, LocaleContextHolder.getLocale());
        Response<Void> response = new Response<>(false, messageUser, null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }




    /**
     * Maneja las excepciones de tipo {@link RoleNotFoundException}, que se lanzan cuando no se encuentra un rol
     * especificado en la base de datos o en el sistema.
     * <p>
     * Este manejador captura los detalles de la excepción, los traduce a un mensaje para el usuario y los registra en los logs
     * para realizar un seguimiento. Luego, responde al cliente con un mensaje de error y un estado HTTP {@code 404 Not Found}.
     * </p>
     *
     * @param ex La excepción {@link RoleNotFoundException} que contiene los detalles sobre el error relacionado con la búsqueda de un rol.
     * @return Una respuesta con un mensaje genérico de error para el usuario y un código de estado HTTP {@code 404 Not Found}.
     */
    @ExceptionHandler({RoleNotFoundException.class})
    public ResponseEntity<Response<Void>> handleRoleNotFoundException(RoleNotFoundException ex) {

        //Se construye mensaje para el log
        String messageLog = messageService.getMessage("exception.roleNotFound.log", new Object[]{ex.getId(), ex.getRole()}, LocaleContextHolder.getLocale());
        log.error(messageLog);

        //Se construye mensaje para el usuario.
        String messageUser = messageService.getMessage("exception.roleNotFound.user", null, LocaleContextHolder.getLocale());
        Response<Void> response = new Response<>(false, messageUser, null);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }



    /**
     * Maneja las excepciones de tipo {@link RoleExistingException}, que se lanzan cuando se intenta crear o actualizar
     * un rol que ya existe en el sistema o en la base de datos.
     * <p>
     * Este manejador captura los detalles de la excepción, los traduce a un mensaje para el usuario y los registra en los logs
     * para realizar un seguimiento. Luego, responde al cliente con un mensaje de error y un estado HTTP {@code 409 Conflict}.
     * </p>
     *
     * @param ex La excepción {@link RoleExistingException} que contiene los detalles sobre el error relacionado con la existencia del rol.
     * @return Una respuesta con un mensaje específico para el usuario y un código de estado HTTP {@code 409 Conflict}.
     */
    @ExceptionHandler({RoleExistingException.class})
    public ResponseEntity<Response<Void>> handleRoleExistingException(RoleExistingException ex) {

        //Se construye mensaje para el log.
        String messageLog = messageService.getMessage("exception.roleExisting.log", new Object[]{ex.getEntityType(), ex.getOperation(), ex.getRole()}, LocaleContextHolder.getLocale());
        log.error(messageLog);

        //Se construye mensaje para el usuario.
        String messageUser = messageService.getMessage("exception.roleExisting.user", new Object[]{ex.getRole()}, LocaleContextHolder.getLocale());
        Response<Void> response = new Response<>(false, messageUser, null);
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }



    /**
     * Maneja las excepciones de tipo {@link PermissionNotFoundRoleCreationException}, que se lanzan cuando no se encuentra
     * un permiso necesario durante la creación de un rol en el sistema.
     * <p>
     * Este manejador captura los detalles de la excepción, los traduce a un mensaje para el usuario y los registra en los logs
     * para realizar un seguimiento. Luego, responde al cliente con un mensaje de error y un estado HTTP {@code 400 Bad Request}.
     * </p>
     *
     * @param ex La excepción {@link PermissionNotFoundRoleCreationException} que contiene los detalles sobre el error relacionado con permisos faltantes.
     * @return Una respuesta con un mensaje específico para el usuario y un código de estado HTTP {@code 400 Bad Request}.
     */
    @ExceptionHandler({PermissionNotFoundRoleCreationException.class})
    public ResponseEntity<Response<Void>> handlePermissionNotFoundRoleCreationException(PermissionNotFoundRoleCreationException ex) {
        //Se construye mensaje para el log
        String messageLog = messageService.getMessage("exception.permissionNotFoundRoleCreationException.log", new Object[]{ex.getEntityType(),ex.getOperation(), ex.getId()}, LocaleContextHolder.getLocale());
        log.error(messageLog);

        //Se construye mensaje para el usuario.
        String messageUser = messageService.getMessage("exception.permissionNotFoundRoleCreationException.user", null, LocaleContextHolder.getLocale());
        Response<Void> response = new Response<>(false, messageUser, null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }




    /**
     * Maneja las excepciones de tipo {@link PermissionNotFoundException}, que se lanzan cuando no se encuentra un permiso
     * necesario en el sistema.
     * <p>
     * Este manejador captura los detalles de la excepción, los traduce a un mensaje para el usuario y los registra en los logs
     * para realizar un seguimiento. Luego, responde al cliente con un mensaje de error y un estado HTTP {@code 404 Not Found}.
     * </p>
     *
     * @param ex La excepción {@link PermissionNotFoundException} que contiene los detalles sobre el error relacionado con permisos no encontrados.
     * @return Una respuesta con un mensaje específico para el usuario y un código de estado HTTP {@code 404 Not Found}.
     */
    @ExceptionHandler({PermissionNotFoundException.class})
    public ResponseEntity<Response<Void>> handlePermissionNotFoundException(PermissionNotFoundException ex) {
        //Se construye mensaje para el log
        String messageLog = messageService.getMessage("exception.permissionNotFound.log", new Object[]{ex.getEntityType(),ex.getOperation(), ex.getId()}, LocaleContextHolder.getLocale());
        log.error(messageLog);

        //Se construye mensaje para el usuario.
        String messageUser = messageService.getMessage("exception.permissionNotFound.user", null, LocaleContextHolder.getLocale());
        Response<Void> response = new Response<>(false, messageUser, null);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
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
     * Maneja la excepción {@link CredentialsException}, que se lanza cuando las credenciales proporcionadas son inválidas
     * durante el proceso de autenticación.
     * <p>
     * Este manejador captura las excepciones de credenciales incorrectas, registra el error junto con la dirección IP del
     * cliente que intenta autenticarse y devuelve un mensaje genérico al usuario con un estado HTTP {@code 401 Unauthorized}.
     * </p>
     *
     * @param ex La excepción {@link CredentialsException} que contiene detalles sobre el error de autenticación.
     * @param request La solicitud HTTP que contiene la información de la dirección IP del cliente que realizó la solicitud.
     * @return Una respuesta con un mensaje personalizado para el usuario y un código de estado HTTP {@code 401 Unauthorized}.
     */
    @ExceptionHandler({CredentialsException.class})
    public ResponseEntity<Response<Void>> handleCredentialsException(CredentialsException ex, HttpServletRequest request) {
        //Obtener IP
        String ip = request.getRemoteAddr();

        // Cargar el mensaje de error para log.
        String logMessage = messageService.getMessage(
                "exception.badCredentials.log", // La clave de la propiedad
                new Object[]{ex.getUsername(),ip}, // Pasamos el username como parámetro
                LocaleContextHolder.getLocale() // Usamos el locale para la localización
        );

        log.error(logMessage);

        // Crear mensaje genérico para el usuario
        String userMessage = messageService.getMessage(
                "exception.badCredentials.user",
                null,
                LocaleContextHolder.getLocale());

        // Crear la respuesta con el mensaje personalizado
        Response<Void> response = new Response<>(false, userMessage, null);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }



    /**
     * Maneja la excepción {@link PasswordMismatchException}, que se lanza cuando las contraseñas proporcionadas no coinciden
     * durante la operación de cambio de contraseña o actualización de usuario.
     * <p>
     * Este manejador captura la excepción de que las contraseñas no coinciden, registra el error en el log incluyendo el nombre
     * del usuario afectado y el creador de la operación, y devuelve un mensaje genérico al usuario con un estado HTTP
     * {@code 409 Conflict}.
     * </p>
     *
     * @param ex La excepción {@link PasswordMismatchException} que contiene detalles sobre el error de las contraseñas que no coinciden.
     * @return Una respuesta con un mensaje personalizado para el usuario y un código de estado HTTP {@code 409 Conflict}.
     */
    @ExceptionHandler (PasswordMismatchException.class)
    public ResponseEntity<Response<Void>> handlePasswordMismatchException(PasswordMismatchException ex) {

        //Se recupera usuario autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userCreador = authentication.getName();

        //Construye mensaje para el Log.
        String logMessage = messageService.getMessage(
                "userService.save.passwordNotEquals.log",
                new Object[]{ ex.getUserNuevo(),userCreador},
                LocaleContextHolder.getLocale());

        //Log Error.
        log.error(logMessage);

        //Se construye mensaje para usuario.
        String userMessage = messageService.getMessage("userService.save.passwordNotEquals.user", null, LocaleContextHolder.getLocale());

        Response<Void> response = new Response<>(false, userMessage, null);
        return new ResponseEntity<>(response,HttpStatus.CONFLICT);

    }



    /**
     * Manejador de excepciones para actualizaciones de usuarios que intentan modificarse a sí mismos.
     * Este método se ejecuta cuando se lanza una excepción de tipo {@link UserUpdateSelfUpdateException}.
     * El manejador captura la excepción, registra el mensaje de error en los logs y devuelve una respuesta con un mensaje
     * amigable para el usuario indicando que la operación de actualización no está permitida en este caso.
     *
     * @param ex La excepción {@link UserUpdateSelfUpdateException} que se ha lanzado cuando un usuario intenta actualizar
     *           sus propios datos, lo cual no está permitido.
     * @return Una {@link ResponseEntity} con un objeto {@link Response} que contiene el mensaje para el usuario
     * y el estado HTTP {@link HttpStatus#CONFLICT} (409) que indica un conflicto durante la operación.
     */
    @ExceptionHandler(UserUpdateSelfUpdateException.class)
    public ResponseEntity<Response<Void>> handleUserUpdateException(UserUpdateSelfUpdateException ex) {
        //Carga el mensaje para el log.
        String logMessage = messageService.getMessage("exception.validateSelfUpdate.log",new Object[]{ex.getEntityType(), ex.getOperation(), ex.getId()}, LocaleContextHolder.getLocale());
        log.error(logMessage);

        //Se construye mensaje para usuario.
        String userMessage = messageService.getMessage("exception.validateSelfUpdate.user", null, LocaleContextHolder.getLocale());

        Response<Void> response = new Response<>(false, userMessage, null);
        return new ResponseEntity<>(response,HttpStatus.CONFLICT);
    }


    /**
     * Manejador de excepciones para la creación o guardado de usuario cuando se le quiere asignar rol de desarrollador.
     * Este método se ejecuta cuando se lanza una excepción de tipo {@link UserSaveNotDevRoleException}.
     * El manejador captura la excepción, registra el mensaje de error en los logs y devuelve una respuesta con un mensaje
     * amigable para el usuario.
     *
     * @param ex La excepción {@link UserSaveNotDevRoleException} que se ha lanzado cuando el rol del usuario no es de desarrollador.
     * @return Una {@link ResponseEntity} con un objeto {@link Response} que contiene el mensaje para el usuario
     *         y el estado HTTP {@link HttpStatus#CONFLICT} (409) que indica un conflicto durante la operación.
     *
     * @see Response
     * @see UserSaveNotDevRoleException
     * @see HttpStatus#CONFLICT
     */
    @ExceptionHandler(UserSaveNotDevRoleException.class)
    public ResponseEntity<Response<Void>> handleUserSaveNotDevRoleException(UserSaveNotDevRoleException ex) {
        //Carga el mensaje para el log.
        String logMessage = messageService.getMessage("exception.save.validateNotDevRole.log",new Object[]{ex.getEntityType(), ex.getOperation(), ex.getId()}, LocaleContextHolder.getLocale());
        log.error(logMessage);

        //Se construye mensaje para usuario.
        String userMessage = messageService.getMessage("exception.save.validateNotDevRole.user", null, LocaleContextHolder.getLocale());

        Response<Void> response = new Response<>(false, userMessage, null);
        return new ResponseEntity<>(response,HttpStatus.CONFLICT);
    }



    /**
     * Manejador de excepciones para la actualización de usuarios cuando el usuario a actualizar poseé un rol de desarrollador o quiere actualizarse a ese rol.
     * Este método se ejecuta cuando se lanza una excepción de tipo {@link UserUpdateNotDevRoleException}.
     * El manejador captura la excepción, registra el mensaje de error en los logs y devuelve una respuesta con un mensaje
     * amigable para el usuario.
     *
     * @param ex La excepción {@link UserUpdateNotDevRoleException} que se ha lanzado cuando el rol del usuario no es de desarrollador.
     * @return Una {@link ResponseEntity} con un objeto {@link Response} que contiene el mensaje para el usuario
     * y el estado HTTP {@link HttpStatus#CONFLICT} (409) que indica un conflicto durante la operación.
     */
    @ExceptionHandler(UserUpdateNotDevRoleException.class)
    public ResponseEntity<Response<Void>> handleUserUpdateNotDevRoleException(UserUpdateNotDevRoleException ex) {
        //Carga el mensaje para el log.
        String logMessage = messageService.getMessage("exception.update.validateNotDevRole.log",new Object[]{ex.getEntityType(), ex.getOperation(), ex.getId()}, LocaleContextHolder.getLocale());
        log.error(logMessage);

        //Se construye mensaje para usuario.
        String userMessage = messageService.getMessage("exception.update.validateNotDevRole.user", null, LocaleContextHolder.getLocale());

        Response<Void> response = new Response<>(false, userMessage, null);
        return new ResponseEntity<>(response,HttpStatus.CONFLICT);
    }


    /**
     * Manejador de excepciones para la actualización de usuarios. Este método se ejecuta cuando se lanza una
     * excepción de tipo {@link UserUpdateException}. El manejador captura la excepción, registra el mensaje de
     * error en los logs y devuelve una respuesta con un mensaje amigable para el usuario.
     *
     * @param ex La excepción {@link UserUpdateException} que se ha lanzado durante la actualización de un usuario.
     * @return Una {@link ResponseEntity} con un objeto {@link Response} que contiene el mensaje para el usuario
     * y el estado HTTP {@link HttpStatus#CONFLICT} (409) que indica un conflicto durante la operación.
     */
    @ExceptionHandler(UserUpdateException.class)
    public ResponseEntity<Response<Void>> handleUserUpdateException(UserUpdateException ex){
        //Carga el mensaje para el log.
        String logMessage = messageService.getMessage("exception.validateUpdateUser.log", new Object[]{ex.getEntityType(), ex.getOperation(), ex.getId()}, LocaleContextHolder.getLocale());
        log.error(logMessage);

        //Se construye mensaje para usuario.
        String userMessage = messageService.getMessage("exception.validateUpdateUser.user", null, LocaleContextHolder.getLocale());

        Response<Void> response = new Response<>(false, userMessage, null);
        return new ResponseEntity<>(response,HttpStatus.CONFLICT);
    }


    /**
     * Maneja la excepción {@link MessageNotFoundException}, que se lanza cuando no se encuentra un mensaje específico
     * durante una operación de actualización.
     * <p>
     * Este manejador captura la excepción de no encontrar el mensaje solicitado, registra el error en el log incluyendo
     * el tipo de entidad, la operación y el ID relacionado, y devuelve un mensaje genérico al usuario con un estado HTTP
     * {@code 404 Not Found}.
     * </p>
     *
     * @param ex La excepción {@link MessageNotFoundException} que contiene detalles sobre el mensaje no encontrado
     *           y los parámetros relacionados con la entidad y operación.
     * @return Una respuesta con un mensaje personalizado para el usuario y un código de estado HTTP {@code 404 Not Found}.
     */
    @ExceptionHandler({MessageNotFoundException.class})
    public ResponseEntity<Response<Void>> handleMessageNotFoundException(MessageNotFoundException ex) {
        //Se construye mensaje para el log
        String messageLog = messageService.getMessage("exception.messageNotFound.log", new Object[]{ex.getEntityType(),ex.getOperation(), ex.getId()}, LocaleContextHolder.getLocale());
        log.error(messageLog);

        //Se construye mensaje para el usuario.
        String messageUser = messageService.getMessage("exception.messageNotFound.user", null, LocaleContextHolder.getLocale());
        Response<Void> response = new Response<>(false, messageUser, null);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
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
