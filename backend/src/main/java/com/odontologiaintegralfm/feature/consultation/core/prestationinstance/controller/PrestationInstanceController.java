package com.odontologiaintegralfm.feature.consultation.core.prestationinstance.controller;

import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessConsultationCreate;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto.PrestationStepResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.service.GetNextStepsPrestationInstanceUseCase;
import com.odontologiaintegralfm.shared.dto.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/prestation-instance")
@Validated
public class PrestationInstanceController {

    private final GetNextStepsPrestationInstanceUseCase getNextStepsPrestationInstanceUseCase;

    PrestationInstanceController(GetNextStepsPrestationInstanceUseCase getNextStepsPrestationInstanceUseCase) {
        this.getNextStepsPrestationInstanceUseCase = getNextStepsPrestationInstanceUseCase;
    }

    @Operation(summary = "Obtener próximos steps de una prestación", description = "Devuelve los próximos steps habilitados para una instancia de prestación en progreso.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Steps obtenidos correctamente"),
            @ApiResponse(responseCode = "400", description = "La prestación no tiene workflow de steps definido."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "404", description = "Instancia de prestación no encontrada o sin steps configurados."),
            @ApiResponse(responseCode = "409", description = "La prestación no está en estado IN_PROGRESS."),
    })
    @GetMapping("/{id}/next-steps")
    @OnlyAccessConsultationCreate
    public ResponseEntity<Response<List<PrestationStepResponseDTO>>> getNextSteps(@PathVariable Long id) {
        List<PrestationStepResponseDTO> result = getNextStepsPrestationInstanceUseCase.execute(id);
        return new ResponseEntity<>(new Response<>(true, null, result), HttpStatus.OK);
    }
}