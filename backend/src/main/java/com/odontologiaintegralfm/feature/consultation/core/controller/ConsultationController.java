package com.odontologiaintegralfm.feature.consultation.core.controller;

import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessAppointmentsManagementCreate;
import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessAppointmentsManagementOrConsultationRead;
import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessConsultationCreate;
import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessConsultationUpdate;
import com.odontologiaintegralfm.feature.consultation.core.dto.*;
import com.odontologiaintegralfm.feature.consultation.core.service.interfaces.IConsultationOdontogramHeaderService;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.feature.consultation.core.service.interfaces.IConsultationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
    private final IConsultationOdontogramHeaderService odontogramHeaderService;

    public ConsultationController(IConsultationService consultationService,
                                  IConsultationOdontogramHeaderService odontogramService) {
        this.consultationService = consultationService;
        this.odontogramHeaderService = odontogramService;
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






    @Operation(summary = "Actualizar estado de consulta", description = "Actualiza el estado de una consulta por flujo natural.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consulta actualizada"),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @PatchMapping("/{idConsultation}/status")
    @OnlyAccessConsultationUpdate
    public ResponseEntity<Response<ConsultationResponseDTO>> update(@PathVariable @NotNull Long idConsultation) {

        Response<ConsultationResponseDTO> response = consultationService.updateStatus(idConsultation);
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







    @Operation(summary = "Crear odontograma", description = "Crea una odontograma.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Odontograma creada"),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @PostMapping("/{idConsultation}/odontogram")
    @OnlyAccessConsultationCreate
    public ResponseEntity<Response<Void>> createOdontogram(@PathVariable @NotNull Long idConsultation,
                                                           @RequestBody @Valid ConsultationOdontogramCreateRequestDTO odontogram) {
        Response<Void> response = odontogramHeaderService.create(idConsultation, odontogram);
        return ResponseEntity
                .created(URI.create("api/consultations/" + idConsultation + "/odontogram"))
                .body(response);
    }




    @Operation(summary = "Actualizar odontograma", description = "Actualiza una odontograma.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Odontograma actualizado"),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @PutMapping("/{idConsultation}/odontogram")
    @OnlyAccessConsultationCreate
    public ResponseEntity<Response<Void>> updateOdontogram(@PathVariable @NotNull Long idConsultation,
                                                           @RequestBody  @Valid ConsultationOdontogramCorrectionRequestDTO correctionRequestDTO) {
        Response<Void> response = odontogramHeaderService.update(idConsultation, correctionRequestDTO);
        return ResponseEntity
                .created(URI.create("api/consultations/" + idConsultation + "/odontogram"))
                .body(response);
    }








    //Hacer get consulta, odontograma
    //get consulta -> tiene que recuperar tratamiento de la consulta mostrar detalle de precios y valor total.
    //POST para registrar pago -> Validar que pague la totalidad.








}
