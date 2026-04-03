package com.odontologiaintegralfm.feature.consultation.catalogs.dto;


import com.odontologiaintegralfm.feature.consultation.catalogs.enums.ProviderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentProviderDTORequest (

        @NotBlank(message = "paymentProviderDTORequest.name.empty")
         String name,

        @NotNull(message = "paymentProviderDTORequest.type.empty")
        ProviderType type
){}
