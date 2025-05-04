package com.odontologiaintegralfm.controller;

import com.odontologiaintegralfm.configuration.securityConfig.annotations.OnlyAdmistratorAndSecretary;
import com.odontologiaintegralfm.dto.PatientCreateRequestDTO;
import com.odontologiaintegralfm.dto.PatientCreateResponseDTO;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.service.interfaces.IPatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * @author [Facundo Palmieri]
 */

@RestController
@PreAuthorize("denyAll()")
@RequestMapping("/api/patient")
public class PatientController {


    @Autowired
    private IPatientService patientService;

    /**
     * Crea un nuevo paciente en el sistema.
     * <p>
     * Requiere el rol <b>AdmistratorAndSecretary</b> para acceder.
     * </p>
     *
     * @param patientCreateRequestDTO Datos del paciente a crear.
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>201 Created</b>:Paciente creado exitosamente.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         <li><b>409 Conflict</b>: Paciente existente en el sistema.</li>
     *         </ul>
     */

    @Operation(summary = "Crear Paciente", description = "Crea un nuevo Paciente en el sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paciente Creado exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "409", description = "Paciente existente en el sistema.")
    })
    @PostMapping
    @OnlyAdmistratorAndSecretary
    public ResponseEntity<Response<PatientCreateResponseDTO>> createPatient (@Valid @RequestBody PatientCreateRequestDTO patientCreateRequestDTO) {
        Response<PatientCreateResponseDTO> response = patientService.create(patientCreateRequestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }



}
