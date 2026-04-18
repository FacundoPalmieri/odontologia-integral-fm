package com.odontologiaintegralfm.feature.consultation.paymentaccount.service;


import com.odontologiaintegralfm.feature.consultation.paymentaccount.dto.PaymentAccountDTOResponse;
import com.odontologiaintegralfm.shared.dto.Response;


import java.util.List;

public interface IPaymentAccountQueryService {

    Response<PaymentAccountDTOResponse> getById (Long id);

    Response<List<PaymentAccountDTOResponse>> getAll ();

}
