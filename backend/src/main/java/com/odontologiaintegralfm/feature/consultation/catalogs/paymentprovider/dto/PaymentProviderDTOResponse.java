package com.odontologiaintegralfm.feature.consultation.catalogs.paymentprovider.dto;


import com.odontologiaintegralfm.feature.consultation.catalogs.paymentprovider.enums.ProviderType;

public record PaymentProviderDTOResponse (
        Long id,
        String name,
        ProviderType type
){}
