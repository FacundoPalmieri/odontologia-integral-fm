package com.odontologiaintegralfm.feature.payment.paymentprovider.dto;


import com.odontologiaintegralfm.feature.payment.paymentprovider.enums.ProviderType;

public record PaymentProviderDTOResponse (
        Long id,
        String name,
        ProviderType type
){}
