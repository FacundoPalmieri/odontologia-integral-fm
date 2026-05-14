package com.odontologiaintegralfm.feature.consultation.core.consultationinstance.controller;

import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessConsultationCreate;
import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.dto.ConsultationInstanceRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.dto.ConsultationInstanceResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.service.CreateConsultationInstanceUseCase;
import com.odontologiaintegralfm.shared.dto.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/consultation-instance")
@Validated
public class ConsultationInstanceController {

    private final CreateConsultationInstanceUseCase createConsultationInstanceUseCase;

    ConsultationInstanceController(CreateConsultationInstanceUseCase createConsultationInstanceUseCase) {
        this.createConsultationInstanceUseCase = createConsultationInstanceUseCase;
    }


    @Operation(summary = "Crear instancia de consulta", description = "Crea una instancia de consulta.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Instancia de consulta creada"),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @PostMapping
    @OnlyAccessConsultationCreate
    public ResponseEntity<Response<ConsultationInstanceResponseDTO>> create(@RequestBody ConsultationInstanceRequestDTO consultationInstanceRequestDTO) {
        Response<ConsultationInstanceResponseDTO> response = createConsultationInstanceUseCase.execute(consultationInstanceRequestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


}
