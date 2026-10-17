package com.odontologiaintegralfm.feature.payment.paymentprovider.dto;


import com.odontologiaintegralfm.feature.payment.paymentprovider.enums.ProviderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentProviderDTORequest (

        @NotBlank(message = "paymentProviderDTORequest.name.empty")
         String name,

        @NotNull(message = "paymentProviderDTORequest.type.empty")
        ProviderType type
){}
