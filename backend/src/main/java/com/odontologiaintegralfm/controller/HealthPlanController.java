package com.odontologiaintegralfm.controller;

import com.odontologiaintegralfm.dto.HealthPlanResponseDTO;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.service.interfaces.IHealthPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;


@RestController
@RequestMapping("/api/health-plans")
@PreAuthorize("denyAll()")
public class HealthPlanController {


    @Autowired
    private IHealthPlanService healthPlanService;

    /**
     * Endpoint para obtener todos los planes de salud.
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

    @Operation(summary = "Obtener Planes de salud", description = "Obtiene el listado de Prepagas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado recuperado exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole(T(com.odontologiaintegralfm.enums.UserRole).Secretaria.name()," +
            "T(com.odontologiaintegralfm.enums.UserRole).Administrador.name())")
    public ResponseEntity<Response<List<HealthPlanResponseDTO>>> getAll(){
        Response<List<HealthPlanResponseDTO>> response = healthPlanService.getAll();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


}
