package com.odontologiaintegralfm.controller;

import com.odontologiaintegralfm.configuration.securityConfig.annotations.OnlyAdmistratorAndSecretary;
import com.odontologiaintegralfm.dto.GenderResponseDTO;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.service.GenderService;
import com.odontologiaintegralfm.service.interfaces.IGenderService;
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
@PreAuthorize("denyAll()")
@RequestMapping("/api/gender")
public class GenderController {

    @Autowired
    private IGenderService genderService;


    /**
     * Lista todos los tipos de Géneros Habilitados.
     * <p>Requiere rol <b>Administrador y Secretaria</b> para acceder.</p>
     * @return ResponseEntity con:
     * <ul>
     *     <li><b>200 OK</b> Lista de Géneros recuperados exitosamente.</li>
     *     <li><b>401 Unauthorized</b>: No autenticado.</li>
     *     <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     * </ul>
     */
    @Operation(summary = "Obtener listado de Géneros", description = "Lista todos los Géneros.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Géneros Encontrados."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @GetMapping("/all")
    @OnlyAdmistratorAndSecretary
    public ResponseEntity<Response<List<GenderResponseDTO>>> getAll(){
        Response<List<GenderResponseDTO>> response = genderService.getAll();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
