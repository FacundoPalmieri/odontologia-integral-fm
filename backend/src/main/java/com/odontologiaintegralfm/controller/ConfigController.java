package com.odontologiaintegralfm.controller;

import com.odontologiaintegralfm.configuration.securityConfig.annotations.OnlyAccessSystemRead;
import com.odontologiaintegralfm.configuration.securityConfig.annotations.OnlyAccessSystemUpdate;
import com.odontologiaintegralfm.dto.*;
import com.odontologiaintegralfm.model.AttachedFileConfig;
import com.odontologiaintegralfm.model.MessageConfig;
import com.odontologiaintegralfm.service.ConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Controlador encargado de manejar la configuración de mensajes, los intentos fallidos de sesión
 * y la expiración de tokens. Todas las operaciones requieren el rol <b>DEV</b> para ser accesibles.
 * <p>
 * Los métodos proporcionados permiten:
 * <ul>
 *   <li>Obtener y actualizar la configuración de mensajes.</li>
 *   <li>Obtener y actualizar los intentos fallidos de sesión.</li>
 *   <li>Obtener y actualizar la expiración del token.</li>
 * </ul>
 * </p>
 * <p>
 * Las respuestas corresponden a los siguientes casos:
 *  <ul>
 * <li><b>200 OK</b>:éxito</li>
 * <li><b>401 Unauthorized</b>: falta de autenticación (401 Unauthorized)</li>
 *  <li><b>403 Forbidden</b>:falta de autorización (403 Forbidden)<</li>
 *  </ul>
 * </p>
 <p>
 * Este controlador utiliza las siguientes operaciones:
 * <ul>
 *   <li><b>GET /api/config/message</b>: Obtiene la configuración de mensajes.</li>
 *   <li><b>PATCH /api/config/message</b>: Actualiza la configuración de un mensaje.</li>
 *   <li><b>GET /api/config/session</b>: Obtiene la cantidad de intentos fallidos de sesión.</li>
 *   <li><b>PATCH /api/config/session</b>: Actualiza la cantidad de intentos fallidos de sesión.</li>
 *   <li><b>GET /api/config/token</b>: Obtiene la expiración del token.</li>
 *   <li><b>PATCH /api/config/token</b>: Actualiza la expiración del token.</li>
 *   <li><b>GET /api/config/token/refresh</b>: Obtiene la expiración del refresh token.</li>
 *   <li><b>PATCH /api/config/token/refresh</b>: Actualiza la expiración del refresh token.</li>
 * </ul>
 * </p>
 * <p>
 * <b>Roles necesarios:</b> <b>DEV</b>
 * </p>
 */
@RestController
@RequestMapping("/api/config")
public class ConfigController {

    @Autowired
    private ConfigService configService;



//////////////////////////////////////////////
// Sección: Configuración de Mensajes
//////////////////////////////////////////////

    /**
     * Obtiene la configuración de mensajes.
     * <p>
     * Requiere <b>PERMISO_SYSTEM_UPDATE</b> para acceder.
     * </p>
     *
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 OK</b>: Listado de mensajes recuperado exitosamente.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         </ul>
     */
    @Operation(summary = "Obtener configuración de mensajes", description = "Obtiene la configuración de mensajes.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de mensajes recuperado exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @GetMapping("/message")
    @OnlyAccessSystemRead
    public ResponseEntity<Response<List<MessageConfig>>> getMessage() {
        Response<List<MessageConfig>> response = configService.getMessage();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /**
     * Actualiza la configuración de un mensaje.
     * <p>
     * Requiere b>PERMISO_SYSTEM_UPDATE</b> para acceder.
     * </p>
     *
     * @param messageRequestDTO Objeto con los datos del mensaje a actualizar.
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 OK</b>: Mensaje actualizado exitosamente.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         <li><b>404 Not Found</b>: Mensaje no encontrado para actualizar.</li>
     *         </ul>
     */
    @Operation(summary = "Actualizar mensaje", description = "Actualiza la configuración de un mensaje.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mensaje actualizado exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "404", description = "Mensaje no encontrado para actualizar.")
    })
    @PatchMapping("/message")
    @OnlyAccessSystemUpdate
    public ResponseEntity<Response<MessageConfig>> updateMessage(@Valid @RequestBody MessageRequestDTO messageRequestDTO) {
        Response<MessageConfig> response =  configService.updateMessage(messageRequestDTO);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



//////////////////////////////////////////////
// Sección: Configuración de intento fallídos
/////////////////////////////////////////////


    /**
     * Obtiene la cantidad de intentos fallidos de sesión.
     * <p>
     * Requiere <b>PERMISO_SYSTEM_UPDATE</b> para acceder.
     * </p>
     *
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 OK</b>: Cantidad de intentos fallidos recuperada exitosamente.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         </ul>
     */
    @Operation(summary = "Obtener intentos fallidos de sesión", description = "Obtiene la cantidad de intentos fallidos de sesión.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cantidad de intentos fallidos recuperada exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso.")
    })
    @GetMapping("/session")
    @OnlyAccessSystemRead
    public ResponseEntity<Response<Integer>> getAttempts() {
        Response<Integer> response =  configService.getAttempts();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /**
     * Actualiza la cantidad de intentos fallidos de sesión.
     * <p>
     * Requiere <b>PERMISO_SYSTEM_UPDATE</b> para acceder.
     * </p>
     *
     * @param failedLoginAttemptsRequestDTO Datos para actualizar los intentos fallidos de sesión.
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 OK</b>: Cantidad de intentos fallidos actualizada exitosamente.</li>
     *         <li><b>400 Bad Resquest</b>: Datos inválidos para actualizar la cantidad de intentos fallidos de sesión.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         </ul>
     */
    @Operation(summary = "Actualizar intentos fallidos de sesión", description = "Actualiza la cantidad de intentos fallidos de sesión.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cantidad de intentos fallidos actualizada exitosamente."),
            @ApiResponse(responseCode = "400", description = "Datos inválidos para actualizar la cantidad de intentos fallidos de sesión."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso.")
    })
    @PatchMapping("/session")
    @OnlyAccessSystemUpdate
    public ResponseEntity<Response<Integer>> updateAttempts(@Valid @RequestBody FailedLoginAttemptsRequestDTO failedLoginAttemptsRequestDTO) {
        Response<Integer> response = configService.updateAttempts(failedLoginAttemptsRequestDTO);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }





//////////////////////////////////////////////
// Sección: Configuración de Token
/////////////////////////////////////////////
    /**
     * Obtiene la expiración del token.
     * <p>
     * Requiere el rol <b>Desarrollador</b> para acceder.
     * </p>
     *
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 OK</b>: Expiración del token recuperada exitosamente.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         </ul>
     */
    @Operation(summary = "Obtener expiración del token", description = "Obtiene la expiración del token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Expiración del token recuperada exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso.")
    })
    @GetMapping ("/token")
    @OnlyAccessSystemRead
    public ResponseEntity<Response<Long>> getTokenExpiration() {
        Response<Long> response = configService.getTokenExpiration();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Actualiza la expiración del token.
     * <p>
     * Requiere <b>PERMISO_SYSTEM_UPDATE</b> para acceder.
     * </p>
     *
     * @param tokenConfigRequestDTO Datos para actualizar la expiración del token.
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 OK</b>: Expiración del token actualizada exitosamente.</li>
     *         <li><b>400 Bad Request</b>: Datos inválidos para actualizar la expiración del token.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         </ul>
     */
    @Operation(summary = "Actualizar expiración del token", description = "Actualiza la expiración del token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Expiración del token actualizada exitosamente."),
            @ApiResponse(responseCode = "400", description = "Datos inválidos para actualizar la expiración del token."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso.")
    })
    @PatchMapping("/token")
    @OnlyAccessSystemUpdate
    public ResponseEntity<Response<Long>> updateTokenExpiration(@Valid @RequestBody TokenConfigRequestDTO tokenConfigRequestDTO) {
         Response<Long> response = configService.updateTokenExpiration(tokenConfigRequestDTO);
         return new ResponseEntity<>(response, HttpStatus.OK);
    }




//////////////////////////////////////////////
// Sección: Configuración de Refresh Token
/////////////////////////////////////////////

    /**
     * Obtiene la expiración del Refresh token.
     * <p>
     * Requiere el rol <b>Desarrollador</b> para acceder.
     * </p>
     *
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 OK</b>: Expiración del Refresh token recuperada exitosamente.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         </ul>
     */
    @Operation(summary = "Obtener expiración del Refresh token", description = "Obtiene la expiración del Refresh token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Expiración del Refresh token recuperada exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso.")
    })
    @GetMapping ("/token/refresh")
    @OnlyAccessSystemRead
    public ResponseEntity<Response<Long>> getRefreshTokenExpiration() {
        Response<Long> response = configService.getRefreshTokenExpiration();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /**
     * Actualiza la expiración del Refresh Token.
     * <p>
     * Requiere el rol <b>DEV</b> para acceder.
     * </p>
     *
     * @param refreshTokenConfigRequestDTO con el tiempo de expiración en días
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 OK</b>: Expiración del Refresh token actualizada exitosamente.</li>
     *         <li><b>400 Bad Request</b>: Datos inválidos para actualizar la expiración del RefreshToken.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         </ul>
     */
    @Operation(summary = "Actualizar expiración del Refresh Token", description = "Actualiza la expiración del Refresh Token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Expiración del Refresh token actualizada exitosamente."),
            @ApiResponse(responseCode = "400", description = "Datos inválidos para actualizar la expiración del Refresh Token."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso.")
    })
    @PatchMapping("/token/refresh")
    @OnlyAccessSystemUpdate
    public ResponseEntity<Response<Long>> updateRefreshTokenExpiration(@Valid @RequestBody RefreshTokenConfigRequestDTO refreshTokenConfigRequestDTO) {
        Response<Long> response = configService.updateRefreshTokenExpiration(refreshTokenConfigRequestDTO);
        return new ResponseEntity<>(response, HttpStatus.OK);

    }





//////////////////////////////////////////////
// Sección: Configuración días mínimos permitidos de baja lógica de archivos adjuntos antes de bajas físicas.
/////////////////////////////////////////////
    /**
     * Obtiene la cantidad de días mínimos permitidos de bajas lógicas de un archivo adjunto.
     * <p>
     * Requiere poseer <b>PERMISO_SYSTEM_UPDATE</b> para acceder.
     * </p>
     */
    @Operation(summary = "Obtiene días mínimos de baja lógica de archivos adjuntos", description = "Obtiene la cantidad de días mínimos permitidos de bajas lógicas de un archivo adjunto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Parametrización obtenida exitosamente."),
            @ApiResponse(responseCode = "404", description = "Parametrización no encontrada"),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso.")
    })
    @GetMapping("/attachedFile")
    @OnlyAccessSystemUpdate
    public ResponseEntity<Response<AttachedFileConfig>> getAttachedFileConfig() {
        Response<AttachedFileConfig> response = configService.getAttachedFileConfig();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /**
     * Actualiza la cantidad de días mínimos permitidos de bajas lógicas de un archivo adjunto.
     * <p>
     * Requiere poseer <b>PERMISO_SYSTEM_UPDATE</b> para acceder.
     * </p>
     */
    @Operation(summary = "Actualiza días mínimos de baja lógica de archivos adjuntos", description = "Actualiza la cantidad de días mínimos permitidos de bajas lógicas de un archivo adjunto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Parametrización actualizada exitosamente."),
            @ApiResponse(responseCode = "404", description = "Parametrización no encontrada"),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso.")
    })
    @PatchMapping("/attachedFile")
    @OnlyAccessSystemUpdate
    public ResponseEntity<Response<AttachedFileConfig>> updateAttachedFileConfig(@Valid @RequestBody AttachedFileConfigRequestDTO attachedFileConfigRequestDTO) {
        Response<AttachedFileConfig> response = configService.updateAttachedFileConfig(attachedFileConfigRequestDTO);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



//////////////////////////////////////////////
// Sección: Configuración de Tareas programadas
/////////////////////////////////////////////

    /**
     * Actualiza la expresión Cron de las tareas programadas
     * <p>
     *   Requiere  <b>PERMISO_SYSTEM_UPDATE</b> para acceder.
     * </p>
     * @param scheduleConfigRequestDTO con la expresión Cron
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 OK</b>: Expresión Cron actualizada exitosamente.</li>
     *         <li><b>400 Bad Request</b>: Datos inválidos para actualizar la expresión cron.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         </ul>
     */
    @Operation(summary = "Actualizar expresión Cron de las tareas programadas", description = "Actualiza la expresión Cron de las tareas programadas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Expresión Cron actualizada exitosamente."),
            @ApiResponse(responseCode = "404", description = "Datos inválidos para actualizar la expresión cron."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso.")
    })
    @PatchMapping("/schedule")
    @OnlyAccessSystemUpdate
    public ResponseEntity<Response<ScheduleConfigResponseDTO>> updateSchedule(@Valid @RequestBody ScheduleConfigRequestDTO scheduleConfigRequestDTO) {
        Response<ScheduleConfigResponseDTO> response = configService.updateSchedule(scheduleConfigRequestDTO);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }




    /**
     * Obtiene todas las expresiones Cron de las tareas programadas
     * <p>
     * Requiere <b>PERMISO_SYSTEM_READ</b> para acceder.
     * </p>
     *
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 OK</b>:  Expresiones cron recuperadas exitosamente.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         </ul>
     */
    @Operation(summary = "Obtener listado de tareas programadas", description = "Obtiene la expresión cron de tareas programadas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "expresiones cron recuperadas exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso.")
    })
    @GetMapping("/all/schedule")
    @OnlyAccessSystemRead
    public ResponseEntity<Response<List<ScheduleConfigResponseDTO>>> getAllSchedule() {
        Response<List<ScheduleConfigResponseDTO>> response = configService.getAllSchedule();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }







}
