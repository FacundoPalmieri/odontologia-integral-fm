package com.odontologiaintegralfm.feature.payment.catalogs.paymentprovider.mapper;

import com.odontologiaintegralfm.feature.payment.catalogs.paymentprovider.dto.PaymentProviderDTORequest;
import com.odontologiaintegralfm.feature.payment.catalogs.paymentprovider.dto.PaymentProviderDTOResponse;
import com.odontologiaintegralfm.feature.payment.catalogs.paymentprovider.model.PaymentProvider;
import org.mapstruct.Mapper;

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
