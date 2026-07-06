package com.odontologiaintegralfm.feature.consultation.catalogs.prestation.controller;

import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessConsultationCreate;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.dto.PrestationTypeResponseDTO;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.service.PrestationTypeService;
import com.odontologiaintegralfm.shared.dto.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/prestation")
public class PrestationController {

    private final PrestationTypeService prestationTypeService;

    PrestationController(PrestationTypeService prestationTypeService) {
        this.prestationTypeService = prestationTypeService;
    }

    @Operation(summary = "Obtener catálogo de prestaciones activas", description = "Lista todas las prestaciones activas con precio vigente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Prestaciones encontradas."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @GetMapping("/all")
    @OnlyAccessConsultationCreate
    public ResponseEntity<Response<List<PrestationTypeResponseDTO>>> getAll() {
        return new ResponseEntity<>(prestationTypeService.getAll(), HttpStatus.OK);
    }
}