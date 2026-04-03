package com.odontologiaintegralfm.feature.consultation.core.service.interfaces;


import com.odontologiaintegralfm.feature.consultation.core.dto.PaymentAccountDTORequest;
import com.odontologiaintegralfm.feature.consultation.core.dto.PaymentAccountDTOResponse;
import com.odontologiaintegralfm.shared.dto.Response;

public interface IPaymentAccountUseCase {

    /** Crea una cuenta para cobros */
    Response<PaymentAccountDTOResponse> execute(PaymentAccountDTORequest paymentAccountDTORequest);


}
