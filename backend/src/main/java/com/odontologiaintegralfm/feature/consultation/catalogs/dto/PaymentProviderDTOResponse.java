package com.odontologiaintegralfm.feature.consultation.catalogs.dto;


import com.odontologiaintegralfm.feature.consultation.catalogs.enums.ProviderType;

public record PaymentProviderDTOResponse (
        Long id,
        String name,
        ProviderType type
){}
