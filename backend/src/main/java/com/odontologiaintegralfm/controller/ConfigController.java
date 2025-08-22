package com.odontologiaintegralfm.controller;

import com.odontologiaintegralfm.configuration.securityConfig.annotations.OnlyAccessSystemRead;
import com.odontologiaintegralfm.configuration.securityConfig.annotations.OnlyAccessSystemUpdate;
import com.odontologiaintegralfm.dto.*;
import com.odontologiaintegralfm.model.MessageConfig;
import com.odontologiaintegralfm.service.interfaces.IConfigService;
import com.odontologiaintegralfm.service.interfaces.ISystemLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
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
    private IConfigService configService;

    @Autowired
    private ISystemLogService systemLogService;

    @Value("${pagination.default-page}")
    private int defaultPage;

    @Value("${pagination.default-size}")
    private int defaultSize;

    @Value("${pagination.default-sortBy}")
    private String defaultSortBy;

    @Value("${pagination.default-direction}")
    private String defaultDirection;




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
// Sección: Configuración de parametrizaciones
/////////////////////////////////////////////

    /**
     * Obtiene todas las parametrizaciones del sistema-
     * <p>
     * Requiere <b>PERMISO_SYSTEM_UPDATE</b> para acceder.
     * </p>
     *
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 OK</b>: Parametrizaciones recuperadas exitosamente.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         </ul>
     */
    @Operation(summary = "Obtener parametrizaciones del sistema", description = "Obtiene todas las parametrizaciones del sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Parametrizaciones recuperadas exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso.")
    })
    @GetMapping("/system-parameters")
    @OnlyAccessSystemRead
    public ResponseEntity<Response<List<SystemParameterResponseDTO>>> getSystemParameters() {
       Response<List<SystemParameterResponseDTO>> response =  configService.getSystemParameter();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /**
     * Actualiza valores de parámetro de sistema
     * <p>
     * Requiere <b>PERMISO_SYSTEM_UPDATE</b> para acceder.
     * </p>
     *
     * @param systemParameterRequestDTO Datos para actualizar el valor del parámetro.
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 OK</b>: Parámetro actualizado exitosamente.</li>
     *         <li><b>400 Bad Resquest</b>: Datos inválidos para actualizar valores.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         </ul>
     */
    @Operation(summary = "Actualizar valores de parámetro de sistema.", description = "Actualiza el valor de un parámetros de sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Valor actualizado exitosamente."),
            @ApiResponse(responseCode = "400", description = "Datos inválidos para actualizar valores."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso.")
    })
    @PatchMapping("/system-parameters")
    @OnlyAccessSystemUpdate
    public ResponseEntity<Response<SystemParameterResponseDTO>> updateSystemParameter(@Valid @RequestBody SystemParameterRequestDTO systemParameterRequestDTO) {
        Response<SystemParameterResponseDTO> response = configService.updateSystemParameter(systemParameterRequestDTO);
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
     * @param scheduleRequestDTO con la expresión Cron
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
    public ResponseEntity<Response<ScheduleResponseDTO>> updateSchedule(@Valid @RequestBody ScheduleRequestDTO scheduleRequestDTO) {
        Response<ScheduleResponseDTO> response = configService.updateSchedule(scheduleRequestDTO);
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
    public ResponseEntity<Response<List<ScheduleResponseDTO>>> getAllSchedule() {
        Response<List<ScheduleResponseDTO>> response = configService.getAllSchedule();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }














//////////////////////////////////////////////
// Sección: Logs
/////////////////////////////////////////////

    /**
     * Obtiene la lista de logs del sistema
     * <p>
     * Requiere PERMISO_SYSTEM_READ
     * </p>

     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 Ok</b>:logs recuperados  exitosamente.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         </ul>
     */

    @Operation(summary = "Listar logs", description = "Listar los log del sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logs recuperados exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @GetMapping("/logs")
    @OnlyAccessSystemRead
    public ResponseEntity<Response<Page<SystemLogResponseDTO>>> getLogs(@RequestParam (required = false) Integer page,
                                                              @RequestParam (required = false) Integer size,
                                                              @RequestParam (required = false) String sortBy,
                                                              @RequestParam (required = false) String direction
                                                              ){
        int pageValue = (page != null) ? page : defaultPage;
        int sizeValue = (size != null) ? size : defaultSize;
        String sortByValue = (sortBy != null) ? sortBy : defaultSortBy;
        String directionValue = (direction != null) ? direction : defaultDirection;

        Response<Page<SystemLogResponseDTO>> response = configService.getLogs(pageValue,sizeValue,sortByValue,directionValue);
        return new ResponseEntity<>(response,HttpStatus.OK);

    }



}
