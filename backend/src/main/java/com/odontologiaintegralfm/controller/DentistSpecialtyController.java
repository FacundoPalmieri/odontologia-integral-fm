package com.odontologiaintegralfm.controller;

import com.odontologiaintegralfm.configuration.securityConfig.annotations.OnlyAdmistratorAndSecretary;
import com.odontologiaintegralfm.dto.DentistSpecialtyResponseDTO;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.model.DentistSpecialty;
import com.odontologiaintegralfm.service.DentistSpecialtyService;
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
@RequestMapping("/api/dentist-Specialty")
@OnlyAdmistratorAndSecretary
public class DentistSpecialtyController {

   @Autowired
   private DentistSpecialtyService dentistSpecialtyService;



    /**
     * Lista todas las especialidades de los odontólogos "habilitadas".
     * <p>Requiere rol <b>AdmistratorAndSecretary</b> para acceder.</p>
     * @return ResponseEntity con:
     * <ul>
     *     <li><b>200 OK</b> Lista de especialidades recuperadas exitosamente.</li>
     *     <li><b>401 Unauthorized</b>: No autenticado.</li>
     *     <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     * </ul>
     */
    @Operation(summary = "Obtener listado de especialidades", description = "Lista todos las especialidades.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Especialidades Encontradas."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @GetMapping("/all")
    public ResponseEntity<Response<List<DentistSpecialtyResponseDTO>>> getAll(){
        Response<List<DentistSpecialtyResponseDTO>> response = dentistSpecialtyService.getAll();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
