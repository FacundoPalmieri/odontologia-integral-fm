package com.odontologiaintegralfm.feature.consultation.core.consultation.controller;

import com.odontologiaintegralfm.configuration.securityconfig.annotations.*;
import com.odontologiaintegralfm.feature.consultation.core.consultation.dto.ConsultationCorrectionRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.consultation.dto.ConsultationHistoryResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.consultation.dto.ConsultationResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.consultation.service.CallPatientUseCase;
import com.odontologiaintegralfm.feature.consultation.core.consultation.service.ConsultationQueryService;
import com.odontologiaintegralfm.feature.consultation.core.consultation.service.CreateConsultationUseCase;
import com.odontologiaintegralfm.feature.consultation.core.consultation.service.DesactivateConsultationUseCase;
import com.odontologiaintegralfm.feature.consultation.core.consultation.service.GetConsultationHistoryUseCase;
import com.odontologiaintegralfm.feature.consultation.core.consultation.service.UpdateConsultationCorrectionUseCase;
import com.odontologiaintegralfm.shared.dto.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * Controlador que representa la gestión de consultas.
 */
@RestController
@RequestMapping("/api/consultation")
@Validated
public class ConsultationController {

    private final CreateConsultationUseCase createConsultationUseCase;
    private final UpdateConsultationCorrectionUseCase updateConsultationCorrectionUseCase;
    private final CallPatientUseCase callPatientUseCase;
    private final ConsultationQueryService consultationQueryService;
    private final DesactivateConsultationUseCase desactivateConsultationUseCase;
    private final GetConsultationHistoryUseCase getConsultationHistoryUseCase;

    public ConsultationController(UpdateConsultationCorrectionUseCase updateConsultationCorrectionUseCase,
                                  CreateConsultationUseCase createConsultationUseCase,
                                  CallPatientUseCase callPatientUseCase,
                                  ConsultationQueryService consultationQueryService,
                                  DesactivateConsultationUseCase desactivateConsultationUseCase,
                                  GetConsultationHistoryUseCase getConsultationHistoryUseCase) {
        this.updateConsultationCorrectionUseCase = updateConsultationCorrectionUseCase;
        this.createConsultationUseCase = createConsultationUseCase;
        this.callPatientUseCase  = callPatientUseCase;
        this.consultationQueryService = consultationQueryService;
        this.desactivateConsultationUseCase = desactivateConsultationUseCase;
        this.getConsultationHistoryUseCase = getConsultationHistoryUseCase;
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
        Response<ConsultationResponseDTO> response = createConsultationUseCase.execute(idAppointment);
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

        Response<ConsultationResponseDTO> response = callPatientUseCase.execute(idConsultation);
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
    public ResponseEntity<Response<ConsultationResponseDTO>> updateCorrection(@PathVariable @NotNull Long idConsultation,@RequestBody ConsultationCorrectionRequestDTO correction) {

        Response<ConsultationResponseDTO> response = updateConsultationCorrectionUseCase.execute(idConsultation,correction);
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
        Response<ConsultationResponseDTO> response = consultationQueryService.getById(idConsultation);
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "Obtener estados de consulta por día", description = "Obtiene el estado actual de todas las consultas del día.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consultas recuperadas"),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @GetMapping
    @OnlyAccessConsultationRead
    public ResponseEntity<Response<List<ConsultationResponseDTO>>> getAllByDate() {
        Response<List<ConsultationResponseDTO>> response = consultationQueryService.getAllByDate();
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
    public ResponseEntity<Response<Void>> disabled(@PathVariable @NotNull Long idConsultation,@RequestBody String observation) {

        Response<Void> response = desactivateConsultationUseCase.execute(idConsultation, observation);
        return ResponseEntity.ok(response);

    }


    @Operation(summary = "Historial de consultas del paciente", description = "Obtiene el historial de consultas habilitadas de un paciente, ordenadas desc por id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial recuperado"),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "404", description = "Paciente no encontrado."),
    })
    @GetMapping("/patient/{idPatient}")
    @OnlyAccessConsultationRead
    public ResponseEntity<Response<List<ConsultationHistoryResponseDTO>>> getHistoryByPatient(@PathVariable @NotNull Long idPatient) {
        Response<List<ConsultationHistoryResponseDTO>> response = getConsultationHistoryUseCase.execute(idPatient);
        return ResponseEntity.ok(response);
    }












    //Hacer get consulta, odontograma
    //get consulta -> tiene que recuperar tratamiento de la consulta mostrar detalle de precios y valor total.
    //POST para registrar pago -> Validar que pague la totalidad.








}
