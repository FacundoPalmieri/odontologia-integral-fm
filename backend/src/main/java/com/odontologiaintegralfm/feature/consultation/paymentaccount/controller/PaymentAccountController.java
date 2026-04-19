package com.odontologiaintegralfm.feature.consultation.paymentaccount.controller;

import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessConfigurationCreate;
import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessConfigurationUpdate;
import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessConsultationCreate;
import com.odontologiaintegralfm.feature.consultation.paymentaccount.dto.PaymentAccountDTORequest;
import com.odontologiaintegralfm.feature.consultation.paymentaccount.dto.PaymentAccountDTOResponse;
import com.odontologiaintegralfm.feature.consultation.paymentaccount.service.*;
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

    private final CreatePaymentAccountUseCase createPaymentAccountUseCase;
    private final PaymentAccountQueryService paymentAccountQueryService;
    private final UpdatePaymentAccountStateUseCase updatePaymentAccountStateUseCase;
    private final ActivatePaymentAccountUseCase activatePaymentAccountUseCase;
    private final DesactivatePaymentAccountUseCase desactivatePaymentAccountUseCase;

    public PaymentAccountController(CreatePaymentAccountUseCase createPaymentAccountUseCase,
                                    PaymentAccountQueryService paymentAccountQueryService,
                                    UpdatePaymentAccountStateUseCase updatePaymentAccountStateUseCase,
                                    ActivatePaymentAccountUseCase activatePaymentAccountUseCase,
                                    DesactivatePaymentAccountUseCase desactivatePaymentAccountUseCase) {
        this.createPaymentAccountUseCase = createPaymentAccountUseCase;
        this.paymentAccountQueryService = paymentAccountQueryService;
        this.updatePaymentAccountStateUseCase = updatePaymentAccountStateUseCase;
        this.activatePaymentAccountUseCase = activatePaymentAccountUseCase;
        this.desactivatePaymentAccountUseCase = desactivatePaymentAccountUseCase;
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
        Response<PaymentAccountDTOResponse> response = createPaymentAccountUseCase.execute(paymentAccountDTORequest);
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





    @Operation(summary = "Deshabilitar cuenta bancaria", description = "Deshabilita una cuenta bancaria.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cuenta deshabilitada exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @PatchMapping("/disabled/{id}")
    @OnlyAccessConsultationCreate
    public ResponseEntity<Response<PaymentAccountDTOResponse>> disabled (@PathVariable @Valid @NotNull Long id){
        Response<PaymentAccountDTOResponse> response = desactivatePaymentAccountUseCase.execute(id);
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "Habilitar cuenta bancaria", description = "Habilita una cuenta bancaria.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cuenta habilitada exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @PatchMapping("/enabled/{id}")
    @OnlyAccessConsultationCreate
    public ResponseEntity<Response<PaymentAccountDTOResponse>> enabled (@PathVariable @Valid @NotNull Long id){
        Response<PaymentAccountDTOResponse> response = activatePaymentAccountUseCase.execute(id);
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "Actualiza cuenta bancaria", description = "Actualizar una cuenta bancaria.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cuenta actualizada"),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @PatchMapping("/{id}")
    @OnlyAccessConfigurationUpdate
    public ResponseEntity<Response<PaymentAccountDTOResponse>> update (@PathVariable Long id,
                                                                       @RequestBody PaymentAccountDTORequest paymentAccountDTORequest){
        Response<PaymentAccountDTOResponse> response = updatePaymentAccountStateUseCase.execute(id, paymentAccountDTORequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }










}
