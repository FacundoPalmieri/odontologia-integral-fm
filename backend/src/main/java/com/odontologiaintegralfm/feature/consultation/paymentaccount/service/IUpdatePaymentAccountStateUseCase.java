package com.odontologiaintegralfm.feature.consultation.paymentaccount.service;


import com.odontologiaintegralfm.feature.consultation.paymentaccount.dto.PaymentAccountDTOResponse;
import com.odontologiaintegralfm.shared.dto.Response;

public interface IUpdatePaymentAccountStateUseCase {

    Response<PaymentAccountDTOResponse> disabled (Long idAccount);

    Response<PaymentAccountDTOResponse> enabled (Long idAccount);

}
