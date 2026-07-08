package com.odontologiaintegralfm.feature.consultation.catalogs.promotion.controller;

import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessConfigurationCreate;
import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessConsultationRead;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.dto.PromotionCreateRequestDTO;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.dto.PromotionResponseDTO;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.service.CreatePromotionUseCase;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.service.PromotionService;
import com.odontologiaintegralfm.shared.dto.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/promotion")
public class PromotionController {

    private final PromotionService promotionService;
    private final CreatePromotionUseCase createPromotionUseCase;

    PromotionController(PromotionService promotionService, CreatePromotionUseCase createPromotionUseCase) {
        this.promotionService = promotionService;
        this.createPromotionUseCase = createPromotionUseCase;
    }

    @Operation(summary = "Obtener catálogo de promociones vigentes", description = "Lista todas las promociones habilitadas y vigentes a la fecha actual.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Promociones encontradas."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @GetMapping("/all")
    @OnlyAccessConsultationRead
    public ResponseEntity<Response<List<PromotionResponseDTO>>> getAll() {
        return new ResponseEntity<>(promotionService.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Crear una promoción", description = "Da de alta una nueva promoción de configuración del consultorio.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Promoción creada."),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o reglas de negocio violadas."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "409", description = "Ya existe una promoción con ese label."),
    })
    @PostMapping
    @OnlyAccessConfigurationCreate
    public ResponseEntity<Response<PromotionResponseDTO>> create(@Valid @RequestBody PromotionCreateRequestDTO dto) {
        return new ResponseEntity<>(createPromotionUseCase.execute(dto), HttpStatus.CREATED);
    }
}