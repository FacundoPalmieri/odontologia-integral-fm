package com.odontologiaintegralfm.controller;

import com.odontologiaintegralfm.configuration.securityConfig.annotations.OnlyAccessConfigurationRead;
import com.odontologiaintegralfm.dto.ActionResponseDTO;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.service.ActionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;


/**
 * Controlador que representa las acciones de seguridad.
 */

@RestController
@RequestMapping("/api/action")
public class ActionController {

    @Autowired
    private ActionService actionService;


    /**
     * Lista todas las acciones disponibles en el sistema.
     * <p>
     * Requiere el rol <b>DEV</b> para acceder.
     * </p>
     *
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 OK</b>: Listado de acciones recuperado exitosamente.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         </ul>
     */
    @Operation(summary = "Obtener listado de acciones", description = "Lista todos las acciones.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado recuperado exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })

    @GetMapping("/all")
    @OnlyAccessConfigurationRead
    public ResponseEntity<Response<List<ActionResponseDTO>>> getAll(){
        Response<List<ActionResponseDTO>> response = actionService.getAll();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
