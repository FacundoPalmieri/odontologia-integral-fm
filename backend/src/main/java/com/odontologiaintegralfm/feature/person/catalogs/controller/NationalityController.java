package com.odontologiaintegralfm.feature.person.catalogs.controller;

import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessConfigurationOrPatientsRead;
import com.odontologiaintegralfm.feature.person.catalogs.dto.NationalityResponseDTO;
import com.odontologiaintegralfm.shared.response.Response;
import com.odontologiaintegralfm.feature.person.catalogs.service.interfaces.INationalityService;
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

@RestController
@RequestMapping("/api/nationality")
public class NationalityController {

    @Autowired
    private INationalityService nationalityService;



    /**
     * Lista todas las Nacionalidades Habilitadas.
     * <p>Requiere rol <b>Administrador y Secretaria</b> para acceder.</p>
     * @return ResponseEntity con:
     * <ul>
     *     <li><b>200 OK</b> Lista de Nacionalidades recuperada exitosamente.</li>
     *     <li><b>401 Unauthorized</b>: No autenticado.</li>
     *     <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     * </ul>
     */
    @Operation(summary = "Obtener listado de Nacionalidades", description = "Lista todas las Nacionalidades.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Nacionalidades Encontradas."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @GetMapping("/all")
    @OnlyAccessConfigurationOrPatientsRead
    public ResponseEntity<Response<List<NationalityResponseDTO>>> getAll() {
        Response<List<NationalityResponseDTO>> response = nationalityService.getAll();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
