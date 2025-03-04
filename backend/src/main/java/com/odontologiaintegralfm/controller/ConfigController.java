package com.odontologiaintegralfm.controller;

import com.odontologiaintegralfm.dto.FailedLoginAttemptsDTO;
import com.odontologiaintegralfm.dto.MessageDTO;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.dto.TokenConfigDTO;
import com.odontologiaintegralfm.model.MessageConfig;
import com.odontologiaintegralfm.service.ConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
 *   <li><b>GET /dev/message/get</b>: Obtiene la configuración de mensajes.</li>
 *   <li><b>PATCH /dev/message/update</b>: Actualiza la configuración de un mensaje.</li>
 *   <li><b>GET /dev/session/get</b>: Obtiene la cantidad de intentos fallidos de sesión.</li>
 *   <li><b>PATCH /dev/session/update</b>: Actualiza la cantidad de intentos fallidos de sesión.</li>
 *   <li><b>GET /dev/token/get</b>: Obtiene la expiración del token.</li>
 *   <li><b>PATCH /dev/token/update</b>: Actualiza la expiración del token.</li>
 * </ul>
 * </p>
 * <p>
 * <b>Roles necesarios:</b> <b>DEV</b>
 * </p>
 */
@RestController
@RequestMapping("/dev")
@PreAuthorize("hasRole('DEV')")
public class ConfigController {
    @Autowired
    private ConfigService configService;


    /**
     * Obtiene la configuración de mensajes.
     * <p>
     * Requiere rol <b>DEV</b> para acceder.
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
    @GetMapping("/message/get")
    public ResponseEntity<Response<List<MessageConfig>>> getMessage() {
        Response<List<MessageConfig>> response = configService.getMessage();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /**
     * Actualiza la configuración de un mensaje.
     * <p>
     * Requiere rol <b>DEV</b> para acceder.
     * </p>
     *
     * @param messageDTO Objeto con los datos del mensaje a actualizar.
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
    @PatchMapping("/message/update")
    public ResponseEntity<Response<MessageConfig>> updateMessage(@Valid @RequestBody MessageDTO messageDTO) {
        Response<MessageConfig> response =  configService.updateMessage(messageDTO);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }




    /**
     * Obtiene la cantidad de intentos fallidos de sesión.
     * <p>
     * Requiere el rol <b>DEV</b> para acceder.
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
    @GetMapping("/session/get")
    public ResponseEntity<Response<Integer>> getAttempts() {
        Response<Integer> response =  configService.getAttempts();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /**
     * Actualiza la cantidad de intentos fallidos de sesión.
     * <p>
     * Requiere el rol <b>DEV</b> para acceder.
     * </p>
     *
     * @param failedLoginAttemptsDTO Datos para actualizar los intentos fallidos de sesión.
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
    @PatchMapping("/session/update")
    public ResponseEntity<Response<Integer>> updateAttempts(@Valid @RequestBody FailedLoginAttemptsDTO failedLoginAttemptsDTO) {
        Response<Integer> response = configService.updateAttempts(failedLoginAttemptsDTO);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /**
     * Obtiene la expiración del token.
     * <p>
     * Requiere el rol <b>DEV</b> para acceder.
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
    @GetMapping ("token/get")
    public ResponseEntity<Response<Long>> getTokenExpiration() {
        Response<Long> response = configService.getTokenExpiration();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Actualiza la expiración del token.
     * <p>
     * Requiere el rol <b>DEV</b> para acceder.
     * </p>
     *
     * @param tokenConfigDTO Datos para actualizar la expiración del token.
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
    @PatchMapping("token/update")
    public ResponseEntity<Response<Long>> updateTokenExpiration(@Valid @RequestBody TokenConfigDTO tokenConfigDTO) {
         Response<Long> response = configService.updateTokenExpiration(tokenConfigDTO);
         return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
