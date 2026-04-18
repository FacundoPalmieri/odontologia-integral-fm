package com.odontologiaintegralfm.feature.consultation.paymentaccount.service;

import com.odontologiaintegralfm.feature.consultation.paymentaccount.dto.PaymentAccountDTOResponse;
import com.odontologiaintegralfm.shared.dto.Response;

public interface IDesactivatePaymentAccountUseCase {

    Response<PaymentAccountDTOResponse> execute(Long idAccount);
}
