package com.odontologiaintegralfm.feature.consultation.core.controller;

import com.odontologiaintegralfm.configuration.securityconfig.annotations.*;
import com.odontologiaintegralfm.feature.consultation.core.dto.*;
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


/**
 * Controlador que representa la gestión de consultas.
 */
@RestController
@RequestMapping("/api/consultation")
@Validated
public class ConsultationController {

    private final IConsultationService consultationService;
;

    public ConsultationController(IConsultationService consultationService) {
        this.consultationService = consultationService;
    }


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






    @Operation(summary = "Paciente ingresa a atención", description = "Actualiza el estado de una consulta por ingreso de atención del paciente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consulta actualizada"),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @PostMapping("/{idConsultation}/callPatient")
    @OnlyAccessConsultationUpdate
    public ResponseEntity<Response<ConsultationResponseDTO>> start(@PathVariable @NotNull Long idConsultation) {

        Response<ConsultationResponseDTO> response = consultationService.callPatient(idConsultation);
        return ResponseEntity.ok(response);

    }


    @Operation(summary = "Corrección estado de consulta", description = "Actualiza el estado de una consulta por corrección.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consulta Corregida"),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @PatchMapping("/{idConsultation}/correction")
    @OnlyAccessConsultationUpdate
    public ResponseEntity<Response<ConsultationResponseDTO>> updateCorrection(@PathVariable @NotNull Long idConsultation, ConsultationCorrectionRequestDTO correction) {

        Response<ConsultationResponseDTO> response = consultationService.updateCorrectionStatus(idConsultation,correction);
        return ResponseEntity.ok(response);

    }




    @Operation(summary = "Obtener consulta", description = "Obtiene una consulta.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consulta recuperada"),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @GetMapping("/{idConsultation}")
    @OnlyAccessConsultationRead
    public ResponseEntity<Response<ConsultationResponseDTO>> getById(@PathVariable @NotNull Long idConsultation) {
        Response<ConsultationResponseDTO> response = consultationService.getById(idConsultation);
        return ResponseEntity.ok(response);
    }







    @Operation(summary = "Elimina una consulta", description = "Elimina una consulta iniciada.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consulta Eliminada"),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @DeleteMapping("/{idConsultation}/disabled")
    @OnlyAccessConsultationUpdate
    public ResponseEntity<Response<Void>> disabled(@PathVariable @NotNull Long idConsultation, String observation) {

        Response<Void> response = consultationService.disabled(idConsultation, observation);
        return ResponseEntity.ok(response);

    }












    //Hacer get consulta, odontograma
    //get consulta -> tiene que recuperar tratamiento de la consulta mostrar detalle de precios y valor total.
    //POST para registrar pago -> Validar que pague la totalidad.








}
