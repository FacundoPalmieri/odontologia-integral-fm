package com.odontologiaintegralfm.feature.consultation.catalogs.service.interfaces;


import com.odontologiaintegralfm.feature.consultation.catalogs.dto.PaymentProviderDTORequest;
import com.odontologiaintegralfm.feature.consultation.catalogs.dto.PaymentProviderDTOResponse;
import com.odontologiaintegralfm.shared.dto.Response;

import java.util.List;

public interface IPaymentProviderService {
    /**
     * Obtiene todas las entidades de pago.
     */
    Response<List<PaymentProviderDTOResponse>> getAll();

}
