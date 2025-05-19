package com.odontologiaintegralfm.controller;

import com.odontologiaintegralfm.configuration.securityConfig.annotations.OnlyAdministrator;
import com.odontologiaintegralfm.configuration.securityConfig.annotations.OnlyAdmistratorAndSecretary;
import com.odontologiaintegralfm.dto.DentistCreateRequestDTO;
import com.odontologiaintegralfm.dto.DentistResponseDTO;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.service.DentistService;
import com.odontologiaintegralfm.service.interfaces.IDentistService;
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

@RestController
@RequestMapping("/api/dentist")
@PreAuthorize("denyAll()")
public class DentistController {


    @Autowired
    private IDentistService dentistService;


    /**
     * Crea un nuevo Odontólogo en el sistema.
     * <p>
     * Requiere el rol <b>Admistrator</b> para acceder.
     * </p>
     *
     * @param dentistRequestDTO Datos del odontólogo a crear.
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>201 Created</b>:Odontólogo creado exitosamente.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         <li><b>409 Conflict</b>: Odontólogo existente en el sistema.</li>
     *         </ul>
     */

    @Operation(summary = "Crear Odontólogo", description = "Crea un nuevo Odontólogo en el sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Odontólogo Creado exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "409", description = "Odontólogo existente en el sistema.")
    })
    @PostMapping
    @OnlyAdministrator
    public ResponseEntity<Response<DentistResponseDTO>> create(@Valid @RequestBody DentistCreateRequestDTO dentistRequestDTO){
        Response<DentistResponseDTO>response = dentistService.create(dentistRequestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    /**
     * Obtiene la lista de todos los Odontólogos habilitados en el sistema.
     * <p>
     * Requiere el rol <b>Admistrator y Secretary</b> para acceder.
     * </p>

     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 Ok</b>:Lista recuperada  exitosamente.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         </ul>
     */

    @Operation(summary = "Listar Odontólogos", description = "Listar los Odontólogos habilitdos en el sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista actualizada exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @GetMapping
    @OnlyAdmistratorAndSecretary
    public ResponseEntity<Response<List<DentistResponseDTO>>> getAll(){
        Response<List<DentistResponseDTO>> response = dentistService.getAll();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }






}
