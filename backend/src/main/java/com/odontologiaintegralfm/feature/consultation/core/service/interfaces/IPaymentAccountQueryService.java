package com.odontologiaintegralfm.feature.consultation.core.service.interfaces;


import com.odontologiaintegralfm.feature.consultation.core.dto.PaymentAccountDTOResponse;
import com.odontologiaintegralfm.shared.dto.Response;


import java.util.List;

public interface IPaymentAccountQueryService {


    Response<PaymentAccountDTOResponse> getById (Long id);

    Response<List<PaymentAccountDTOResponse>> getAll ();

}
