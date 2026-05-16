package com.odontologiaintegralfm.feature.payment.catalogs.paymentprovider.controller;

import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessConsultationRead;
import com.odontologiaintegralfm.feature.payment.catalogs.paymentprovider.dto.PaymentProviderDTOResponse;
import com.odontologiaintegralfm.feature.payment.catalogs.paymentprovider.service.PaymentProviderService;
import com.odontologiaintegralfm.shared.dto.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar los proveedores de pago (PaymentProvider).
 */

@RestController
@Validated
@RequestMapping("/api/payment-providers")
public class PaymentProviderController {

    private final PaymentProviderService paymentProviderService;

    public PaymentProviderController(PaymentProviderService paymentProviderService) {
        this.paymentProviderService = paymentProviderService;
    }



    @Operation(summary = "Obtener entidades de pagos", description = "Obtiene entidades de pago.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Entidades obtenidos correctamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @GetMapping("/all")
    @OnlyAccessConsultationRead
    public ResponseEntity<Response<List<PaymentProviderDTOResponse>>> getAll(){
        Response<List<PaymentProviderDTOResponse>> response = paymentProviderService.getAll();
        return ResponseEntity.ok(response);
    }


}
