package com.odontologiaintegralfm.feature.consultation.core.controller;

import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessConfigurationCreate;
import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessConsultationCreate;
import com.odontologiaintegralfm.feature.consultation.core.dto.PaymentAccountDTORequest;
import com.odontologiaintegralfm.feature.consultation.core.dto.PaymentAccountDTOResponse;
import com.odontologiaintegralfm.feature.consultation.core.service.interfaces.IPaymentAccountUseCase;
import com.odontologiaintegralfm.feature.consultation.core.service.interfaces.IPaymentAccountQueryService;
import com.odontologiaintegralfm.shared.dto.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/payment-account")
public class PaymentAccountController {

    private final IPaymentAccountUseCase paymentAccountUseCase;
    private final IPaymentAccountQueryService paymentAccountQueryService;

    public PaymentAccountController(IPaymentAccountUseCase paymentAccountUseCase,IPaymentAccountQueryService paymentAccountQueryService) {
        this.paymentAccountUseCase = paymentAccountUseCase;
        this.paymentAccountQueryService = paymentAccountQueryService;
    }


    @Operation(summary = "Crear cuenta bancaria", description = "Crea una cuenta bancaria.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "cuenta creada"),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @PostMapping
    @OnlyAccessConfigurationCreate
    public ResponseEntity<Response<PaymentAccountDTOResponse>> create (@RequestBody PaymentAccountDTORequest paymentAccountDTORequest){
        Response<PaymentAccountDTOResponse> response = paymentAccountUseCase.execute(paymentAccountDTORequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }



    @Operation(summary = "Obtener cuenta bancaria", description = "Obtener una cuenta bancaria.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cuenta obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @GetMapping("/{id}")
    @OnlyAccessConsultationCreate
    public ResponseEntity<Response<PaymentAccountDTOResponse>> getById (@PathVariable @Valid @NotNull Long id){
        Response<PaymentAccountDTOResponse> response = paymentAccountQueryService.getById(id);
        return ResponseEntity.ok(response);
    }



    @Operation(summary = "Listar todas las cuentas bancarias", description = "Lista todas las cuentas bancarias.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cuentas obtenidas exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @GetMapping("/all")
    @OnlyAccessConsultationCreate
    public ResponseEntity<Response<List<PaymentAccountDTOResponse>>> getAll (){
        Response<List<PaymentAccountDTOResponse>> response = paymentAccountQueryService.getAll();
        return ResponseEntity.ok(response);
    }






}
