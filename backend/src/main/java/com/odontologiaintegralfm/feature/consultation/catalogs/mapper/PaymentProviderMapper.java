package com.odontologiaintegralfm.feature.consultation.catalogs.mapper;

import com.odontologiaintegralfm.feature.consultation.catalogs.dto.PaymentProviderDTORequest;
import com.odontologiaintegralfm.feature.consultation.catalogs.dto.PaymentProviderDTOResponse;
import com.odontologiaintegralfm.feature.consultation.catalogs.model.PaymentProvider;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentProviderMapper {

    // De entidad a DTO Response
    PaymentProviderDTOResponse toDTO(PaymentProvider paymentProvider);

    // De DTO Request a entidad
    PaymentProvider toEntity(PaymentProviderDTORequest dto);

    // Para listas
    List<PaymentProviderDTOResponse> toDTOList(List<PaymentProvider> paymentProviders);
}
