package com.odontologiaintegralfm.feature.consultation.core.mapper;


import com.odontologiaintegralfm.feature.consultation.catalogs.mapper.PaymentProviderMapper;
import com.odontologiaintegralfm.feature.consultation.core.dto.PaymentAccountDTORequest;
import com.odontologiaintegralfm.feature.consultation.core.dto.PaymentAccountDTOResponse;
import com.odontologiaintegralfm.feature.consultation.core.model.PaymentAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

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


}
