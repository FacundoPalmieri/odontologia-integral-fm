package com.odontologiaintegralfm.controller;

import com.odontologiaintegralfm.configuration.securityConfig.annotations.OnlyAdmistratorAndSecretary;
import com.odontologiaintegralfm.dto.DniTypeResponseDTO;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.service.interfaces.IDniTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Set;

/**
 * @author [Facundo Palmieri]
 */
@RestController
@OnlyAdmistratorAndSecretary
@RequestMapping("/api/dni-type")
public class DniTypeController {
    @Autowired
    private IDniTypeService dniTypeService;


    /**
     * Endpoint para obtener todos los tipos de Identificaciones Habilitadas
     * <p>
     * Requiere el rol <b>Administrador y Secretaria</b> para acceder.
     * </p>
     @return ResponseEntity con:
      *         <ul>
      *         <li><b>200 OK</b>: Datos Obtenidos exitosamente.</li>
      *         <li><b>401 Unauthorized</b>: No autenticado.</li>
      *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
      *         </ul>
     */

    @Operation(summary = "Obtener Tipos de identificación", description = "Obtiene el listado de Identificaciones")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado recuperado exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @GetMapping("/all")
    public ResponseEntity<Response<Set<DniTypeResponseDTO>>> getAll(){
        Response<Set<DniTypeResponseDTO>> response = dniTypeService.getAll();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
