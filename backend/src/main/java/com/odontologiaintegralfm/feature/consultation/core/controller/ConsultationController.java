package com.odontologiaintegralfm.feature.consultation.core.controller;

import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessAppointmentsManagementCreate;
import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessConsultationCreate;
import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessConsultationUpdate;
import com.odontologiaintegralfm.feature.consultation.core.dto.ConsultationResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.dto.ConsultationUpdateRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.dto.ToothDTO;
import com.odontologiaintegralfm.feature.consultation.core.service.interfaces.IConsultationOdontogramService;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.feature.consultation.core.service.interfaces.IConsultationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;


/**
 * Controlador que representa la gestión de consultas.
 */
@RestController
@RequestMapping("/api/consultation")
@Validated
public class ConsultationController {

    private final IConsultationService consultationService;
    private final IConsultationOdontogramService odontogramService;

    public ConsultationController(IConsultationService consultationService,
                                  IConsultationOdontogramService odontogramService) {
        this.consultationService = consultationService;
        this.odontogramService = odontogramService;
    }

    /**
     * Endpoint que permite crear una nueva consulta.
     * <p>Requiere permiso de <b>Consulta creación</b> para acceder.</p>
     * @param idAppointment
     * @return ResponseEntity con:
     * <ul>
     *     <li><b>200 OK</b> Consulta creada exitosamente.</li>
     *     <li><b>401 Unauthorized</b>: No autenticado.</li>
     *     <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     * </ul>
     */
    @Operation(summary = "Crear consulta", description = "Crea una consulta.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consulta creada"),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @PostMapping("/{idAppointment}")
    @OnlyAccessAppointmentsManagementCreate
    public ResponseEntity<Response<ConsultationResponseDTO>> create (@PathVariable @NotNull Long idAppointment){
        Response<ConsultationResponseDTO> response = consultationService.create(idAppointment);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }





    @Operation(summary = "Crear odontograma", description = "Crea una odontograma.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Odontograma creada"),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @PostMapping("/{idConsultation}/odontogram")
    @OnlyAccessConsultationCreate
    public ResponseEntity<Response<Void>> createOdontogram(@PathVariable @NotNull Long idConsultation,
                                                           @RequestBody @Valid List<ToothDTO> odontogram) {
        Response<Void> response = odontogramService.createOdontogram(idConsultation, odontogram);
        return ResponseEntity
                .created(URI.create("api/consultations/" + idConsultation + "/odontogram"))
                .body(response);
    }


    @Operation(summary = "Llamar paciente", description = "Dentista llama al paciente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Llamado creado"),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @PatchMapping("/{idConsultation}/status")
    @OnlyAccessConsultationUpdate
    public ResponseEntity<Response<ConsultationResponseDTO>> update(@PathVariable @NotNull Long idConsultation,
                                                                    @RequestBody @Valid ConsultationUpdateRequestDTO request) {

        Response<ConsultationResponseDTO> response = consultationService.updateStatus(idConsultation, request);
        return ResponseEntity.ok(response);

    }



}
