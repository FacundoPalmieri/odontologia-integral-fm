package com.odontologiaintegralfm.feature.consultation.paymentaccount.mapper;


import com.odontologiaintegralfm.feature.consultation.catalogs.mapper.PaymentProviderMapper;
import com.odontologiaintegralfm.feature.consultation.paymentaccount.model.PaymentAccount;
import com.odontologiaintegralfm.feature.consultation.paymentaccount.dto.PaymentAccountDTORequest;
import com.odontologiaintegralfm.feature.consultation.paymentaccount.dto.PaymentAccountDTOResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = PaymentProviderMapper.class)
public interface PaymentAccountMapper {

    //De DTO a Entidad.
    @Mapping(target = "provider", ignore = true)
    PaymentAccount toEntity (PaymentAccountDTORequest dto);

    //De Entidad a DTO Response
    PaymentAccountDTOResponse toDTO (PaymentAccount entity);

    //De Entidad a DTO Response (Lista)
    List<PaymentAccountDTOResponse> toDTOList (List<PaymentAccount> entities);


    // Para actualización: modifica la entidad existente
    void updateEntityFromDto(PaymentAccountDTORequest dto, @MappingTarget PaymentAccount entity);


}
