package com.odontologiaintegralfm.feature.consultation.core.consultationinstance.controller;

import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessConsultationInstanceCreate;
import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessConsultationRead;
import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessConsultationUpdate;
import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.dto.ConsultationInstanceCreateRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.service.CreateConsultationInstanceUseCase;
//import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.service.GetConsultationInstanceUseCase;
//import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.service.UpdateConsultationInstanceUseCase;
import com.odontologiaintegralfm.shared.dto.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/consultation-instance")
@Validated
public class ConsultationInstanceController {

    private final CreateConsultationInstanceUseCase createConsultationInstanceUseCase;
//    private final GetConsultationInstanceUseCase getConsultationInstanceUseCase;
//    private final UpdateConsultationInstanceUseCase updateConsultationInstanceUseCase;

    ConsultationInstanceController(CreateConsultationInstanceUseCase createConsultationInstanceUseCase
//                                   , GetConsultationInstanceUseCase getConsultationInstanceUseCase,
//                                   UpdateConsultationInstanceUseCase updateConsultationInstanceUseCase
    ) {
        this.createConsultationInstanceUseCase = createConsultationInstanceUseCase;
//        this.getConsultationInstanceUseCase = getConsultationInstanceUseCase;
//        this.updateConsultationInstanceUseCase = updateConsultationInstanceUseCase;
    }


    @Operation(summary = "Crear instancia de consulta", description = "Crea una instancia de consulta.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Instancia de consulta creada"),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @PostMapping
    @OnlyAccessConsultationInstanceCreate
    public ResponseEntity<Response<Long>> create(@Valid @RequestBody ConsultationInstanceCreateRequestDTO consultationInstanceRequestDTO) {
        Response<Long> response = createConsultationInstanceUseCase.execute(consultationInstanceRequestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

        /*
    @Operation(summary = "Obtener detalle de instancia de consulta", description = "Devuelve el detalle de una instancia de consulta por ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Instancia de consulta encontrada"),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "404", description = "Instancia de consulta no encontrada."),
    })
    @GetMapping("/{id}")
    @OnlyAccessConsultationRead
    public ResponseEntity<Response<ConsultationInstanceResponseDTO>> getById(@PathVariable Long id) {
        Response<ConsultationInstanceResponseDTO> response = getConsultationInstanceUseCase.execute(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @Operation(summary = "Corregir instancia de consulta", description = "Corrige el contenido de una instancia de consulta dentro de la ventana de tiempo permitida.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Instancia de consulta corregida"),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "404", description = "Instancia de consulta no encontrada."),
            @ApiResponse(responseCode = "409", description = "Ventana de corrección vencida u otro conflicto de estado."),
    })
    @PutMapping("/{idConsultationInstance}")
    @OnlyAccessConsultationUpdate
    public ResponseEntity<Response<ConsultationInstanceBasicResponseDTO>> update(@PathVariable Long idConsultationInstance,
                                                                             @Valid @RequestBody ConsultationInstanceUpdateRequestDTO consultationInstanceUpdateRequestDTO) {
        Response<ConsultationInstanceBasicResponseDTO> response = updateConsultationInstanceUseCase.execute(idConsultationInstance, consultationInstanceUpdateRequestDTO);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


     */
}
