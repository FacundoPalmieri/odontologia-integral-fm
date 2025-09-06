package com.odontologiaintegralfm.feature.patient.core.controller;

import com.odontologiaintegralfm.configuration.securityconfig.annotations.*;
import com.odontologiaintegralfm.feature.patient.core.dto.PatientCreateRequestDTO;
import com.odontologiaintegralfm.feature.patient.core.dto.PatientUpdateRequestDTO;
import com.odontologiaintegralfm.feature.patient.core.dto.PatientResponseDTO;
import com.odontologiaintegralfm.shared.response.Response;
import com.odontologiaintegralfm.feature.patient.core.service.interfaces.IPatientService;
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


@RestController
@RequestMapping("/api/patient")
public class PatientController {

    @Autowired
    private IPatientService patientService;

    @Value("${pagination.default-page}")
    private int defaultPage;

    @Value("${pagination.default-size}")
    private int defaultSize;

    @Value("${pagination.default.patient-sortBy}")
    private String defaultSortBy;

    @Value("${pagination.default-direction}")
    private String defaultDirection;

    /**
     * Crea un nuevo paciente en el sistema.
     * <p>
     * Requiere el rol <b>AdmistratorAndSecretary</b> para acceder.
     * </p>
     *
     * @param patientRequestDTO Datos del paciente a crear.
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
            @ApiResponse(responseCode = "201", description = "Paciente Creado exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "409", description = "Paciente existente en el sistema.")
    })
    @PostMapping
    @OnlyAccessPatientsCreate
    public ResponseEntity<Response<PatientResponseDTO>> create(@Valid @RequestBody PatientCreateRequestDTO patientRequestDTO) {
        Response<PatientResponseDTO> response = patientService.create(patientRequestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }



    /**
     * Actualiza un paciente en el sistema.
     * <p>
     * Requiere el rol <b>AdmistratorAndSecretary</b> para acceder.
     * </p>
     *
     * @param patientUpdateRequestDTO Datos del paciente a actualizar.
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 Ok</b>:Paciente actualizado exitosamente.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         <li><b>409 Conflict</b>: Paciente existente en el sistema.</li>
     *         </ul>
     */

    @Operation(summary = "Actualizar Paciente", description = "Actualizar un  Paciente en el sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paciente actualizado exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "409", description = "Paciente existente en el sistema.")
    })
    @PatchMapping
    @OnlyAccessPatientsUpdate
    public ResponseEntity<Response<PatientResponseDTO>> update(@Valid @RequestBody PatientUpdateRequestDTO patientUpdateRequestDTO) {
        Response<PatientResponseDTO> response = patientService.update(patientUpdateRequestDTO);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /**
     * Obtiene la lista de todos los pacientes habilitados en el sistema.
     * <p>
     * Requiere estár autenticado para acceder.
     * </p>

     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 Ok</b>:Lista recuperada  exitosamente.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         </ul>
     */

    @Operation(summary = "Listar Pacientes", description = "Listar los Paciente habilitdos en el sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista actualizada exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @GetMapping("/all")
    @OnlyAccessPatientsRead
    public ResponseEntity<Response<Page<PatientResponseDTO>>> getAll(@RequestParam(required = false) Integer page,
                                                                     @RequestParam(required = false) Integer size,
                                                                     @RequestParam(required = false) String  sortBy,
                                                                     @RequestParam(required = false) String  direction) {
        int pageValue = (page != null) ? page : defaultPage;
        int sizeValue = (size != null) ? size : defaultSize;
        String sortByValue = (sortBy != null) ? sortBy : defaultSortBy;
        String directionValue = (direction != null) ? direction : defaultDirection;

        Response<Page<PatientResponseDTO>> response = patientService.getAll(pageValue,sizeValue, sortByValue,directionValue);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /**
     /**
     * Obtiene paciente con estado habilitado por su ID.
     * <p>
     * Requiere estar autenticado para acceder.
     * </p>
     *
     * @param id ID del paciente a buscar.
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 OK</b>: Paciente encontrado exitosamente.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         <li><b>404 Not Found</b>: Paciente no encontrado.</li>
     *         </ul>
     */
    @Operation(summary = "Obtener Paciente", description = "Obtiene un Paciente por su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paciente encontrado exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "404", description = "Paciente no encontrado.")
    })
    @GetMapping("/{id}")
    @OnlyAccessPatientsRead
    public ResponseEntity<Response<PatientResponseDTO>> getById(@PathVariable Long id) {
        Response<PatientResponseDTO> response = patientService.getById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

}
