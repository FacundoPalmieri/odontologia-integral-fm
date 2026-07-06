package com.odontologiaintegralfm.feature.consultation.core.payment.controller;

import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessPaymentCreate;
import com.odontologiaintegralfm.feature.consultation.core.payment.dto.PaymentRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.payment.dto.PaymentResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.payment.service.RegisterPaymentUseCase;
import com.odontologiaintegralfm.shared.dto.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment")
@Validated
public class PaymentController {

    private final RegisterPaymentUseCase registerPaymentUseCase;

    PaymentController(RegisterPaymentUseCase registerPaymentUseCase) {
        this.registerPaymentUseCase = registerPaymentUseCase;
    }

    @Operation(summary = "Registrar pago de prestación", description = "Registra un pago parcial o total sobre una prestación.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pago registrado correctamente."),
            @ApiResponse(responseCode = "400", description = "Monto inválido."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "404", description = "Prestación no encontrada."),
            @ApiResponse(responseCode = "409", description = "Deuda ya saldada, monto excede deuda, o pago duplicado."),
    })
    @PostMapping
    @OnlyAccessPaymentCreate
    public ResponseEntity<Response<PaymentResponseDTO>> create(@Valid @RequestBody PaymentRequestDTO dto) {
        Response<PaymentResponseDTO> response = registerPaymentUseCase.execute(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
