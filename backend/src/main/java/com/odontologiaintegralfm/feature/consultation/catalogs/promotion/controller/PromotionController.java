package com.odontologiaintegralfm.feature.consultation.catalogs.promotion.controller;

import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessConfigurationCreate;
import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessConfigurationRead;
import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessConfigurationUpdate;
import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessConsultationRead;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.dto.PromotionCreateRequestDTO;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.dto.PromotionResponseDTO;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.dto.PromotionUpdateRequestDTO;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.service.*;
import com.odontologiaintegralfm.shared.dto.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/promotion")
public class PromotionController {

    private final PromotionQueryService promotionQueryService;
    private final CreatePromotionUseCase createPromotionUseCase;
    private final UpdatePromotionUseCase updatePromotionUseCase;
    private final EnablePromotionUseCase enablePromotionUseCase;
    private final DisablePromotionUseCase disablePromotionUseCase;

    PromotionController(PromotionQueryService promotionQueryService, CreatePromotionUseCase createPromotionUseCase, UpdatePromotionUseCase updatePromotionUseCase, EnablePromotionUseCase enablePromotionUseCase, DisablePromotionUseCase disablePromotionUseCase) {
        this.promotionQueryService = promotionQueryService;
        this.createPromotionUseCase = createPromotionUseCase;
        this.updatePromotionUseCase = updatePromotionUseCase;
        this.enablePromotionUseCase = enablePromotionUseCase;
        this.disablePromotionUseCase = disablePromotionUseCase;
    }

    @Operation(summary = "Obtener catálogo de promociones vigentes", description = "Lista todas las promociones habilitadas y vigentes a la fecha actual.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Promociones encontradas."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @GetMapping("/current")
    @OnlyAccessConsultationRead
    public ResponseEntity<Response<List<PromotionResponseDTO>>> getCurrent() {
        return new ResponseEntity<>(promotionQueryService.getCurrent(), HttpStatus.OK);
    }

    @Operation(summary = "Obtener catálogo administrativo de promociones", description = "Lista todas las promociones habilitadas, sin filtro de vigencia por fecha (vencidas, futuras y vigentes).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Promociones encontradas."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @GetMapping("/all")
    @OnlyAccessConfigurationRead
    public ResponseEntity<Response<List<PromotionResponseDTO>>> getAll() {
        return new ResponseEntity<>(promotionQueryService.getAll(), HttpStatus.OK);
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

    @Operation(summary = "Editar una promoción", description = "Actualiza una promoción existente. El front envía el objeto completo; la API ignora los campos iguales al persistido y valida/rechaza los distintos según el estado actual de la promoción (No iniciada/Vigente/Finalizada).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Promoción actualizada (o sin cambios, si el objeto enviado es idéntico al persistido)."),
            @ApiResponse(responseCode = "400", description = "Fecha anterior a hoy o value/discountType fuera de rango."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "404", description = "No se encontró la promoción."),
            @ApiResponse(responseCode = "409", description = "Campo no permitido según el estado actual de la promoción, o label duplicado."),
    })
    @PutMapping("/{id}")
    @OnlyAccessConfigurationUpdate
    public ResponseEntity<Response<PromotionResponseDTO>> update(@PathVariable @Valid @NotNull Long id, @Valid @RequestBody PromotionUpdateRequestDTO dto) {
        return ResponseEntity.ok(updatePromotionUseCase.execute(id, dto));
    }

    @Operation(summary = "Habilitar una promoción", description = "Habilita una promoción previamente deshabilitada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Promoción habilitada."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "404", description = "No se encontró la promoción."),
            @ApiResponse(responseCode = "409", description = "La promoción ya se encuentra habilitada."),
    })
    @PatchMapping("/enabled/{id}")
    @OnlyAccessConfigurationUpdate
    public ResponseEntity<Response<PromotionResponseDTO>> enable(@PathVariable @Valid @NotNull Long id) {
        return ResponseEntity.ok(enablePromotionUseCase.execute(id));
    }

    @Operation(summary = "Deshabilitar una promoción", description = "Deshabilita una promoción previamente habilitada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Promoción deshabilitada."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "404", description = "No se encontró la promoción."),
            @ApiResponse(responseCode = "409", description = "La promoción ya se encuentra deshabilitada."),
    })
    @PatchMapping("/disabled/{id}")
    @OnlyAccessConfigurationUpdate
    public ResponseEntity<Response<PromotionResponseDTO>> disable(@PathVariable @Valid @NotNull Long id) {
        return ResponseEntity.ok(disablePromotionUseCase.execute(id));
    }
}