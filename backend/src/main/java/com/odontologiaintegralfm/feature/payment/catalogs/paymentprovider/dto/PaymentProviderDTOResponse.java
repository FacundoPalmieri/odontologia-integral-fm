package com.odontologiaintegralfm.feature.payment.catalogs.paymentprovider.dto;


import com.odontologiaintegralfm.feature.payment.catalogs.paymentprovider.enums.ProviderType;

public record PaymentProviderDTOResponse (
        Long id,
        String name,
        ProviderType type
){}
