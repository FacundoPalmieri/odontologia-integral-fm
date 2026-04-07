package com.odontologiaintegralfm.feature.consultation.paymentaccount.service;


import com.odontologiaintegralfm.feature.consultation.paymentaccount.dto.PaymentAccountDTORequest;
import com.odontologiaintegralfm.feature.consultation.paymentaccount.dto.PaymentAccountDTOResponse;
import com.odontologiaintegralfm.shared.dto.Response;

public interface ICreatePaymentAccountUseCase {

    /** Crea una cuenta para cobros */
    Response<PaymentAccountDTOResponse> execute(PaymentAccountDTORequest paymentAccountDTORequest);


}
