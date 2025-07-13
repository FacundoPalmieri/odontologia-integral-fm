package com.odontologiaintegralfm.controller;

import com.odontologiaintegralfm.configuration.securityConfig.annotations.OnlyAdmistratorAndSecretary;
import com.odontologiaintegralfm.dto.MedicalRiskResponseDTO;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.service.MedicalRiskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@OnlyAdmistratorAndSecretary
@RequestMapping("/api/medical-risk")
public class MedicalRiskController {
    @Autowired
    private MedicalRiskService medicalRiskService;

    /**
     * Lista todos los tipos de riesgos médicos Habilitados.
     * <p>Requiere rol <b>Administrador y Secretaria</b> para acceder.</p>
     * @return ResponseEntity con:
     * <ul>
     *     <li><b>200 OK</b> Lista de riesgos médicos recuperados exitosamente.</li>
     *     <li><b>401 Unauthorized</b>: No autenticado.</li>
     *     <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     * </ul>
     */
    @Operation(summary = "Obtener listado de riesgos médicos", description = "Lista todos los riesgos médicos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "riesgos médicos recuperados exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @GetMapping("/all")
    public ResponseEntity<Response<Set<MedicalRiskResponseDTO>>> getAll(){
        Response<Set<MedicalRiskResponseDTO>> response = medicalRiskService.getAll();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
