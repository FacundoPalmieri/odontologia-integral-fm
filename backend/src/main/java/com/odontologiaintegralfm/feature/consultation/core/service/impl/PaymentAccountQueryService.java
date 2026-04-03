package com.odontologiaintegralfm.feature.consultation.core.service.impl;


import com.odontologiaintegralfm.feature.consultation.core.dto.PaymentAccountDTOResponse;
import com.odontologiaintegralfm.feature.consultation.core.mapper.PaymentAccountMapper;
import com.odontologiaintegralfm.feature.consultation.core.model.PaymentAccount;
import com.odontologiaintegralfm.feature.consultation.core.repository.IPaymentAccountRepository;
import com.odontologiaintegralfm.feature.consultation.core.service.interfaces.IPaymentAccountQueryService;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentAccountQueryService implements IPaymentAccountQueryService {

    private final IPaymentAccountRepository paymentAccountRepository;
    private final PaymentAccountMapper paymentAccountMapper;

    public PaymentAccountQueryService(IPaymentAccountRepository paymentAccountRepository,PaymentAccountMapper paymentAccountMapper) {
        this.paymentAccountRepository = paymentAccountRepository;
        this.paymentAccountMapper = paymentAccountMapper;
    }


    @Override
    public Response<PaymentAccountDTOResponse> getById(Long id) {

        PaymentAccount paymentAccount = paymentAccountRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("exception.paymentAccountQueryService.notfound.user",null,"exception.paymentAccountQueryService.notfound.log", new Object[]{id,"PaymentAccountQueryService", "getById"}, LogLevel.ERROR));


        return new Response<>(
                true,
                null,
                paymentAccountMapper.toDTO(paymentAccount)
        );
    }



    @Override
    public Response<List<PaymentAccountDTOResponse>> getAll() {

        List<PaymentAccount> paymentAccounts = paymentAccountRepository.findAll();

        return new Response<>(
                true,
                null,
                paymentAccountMapper.toDTOList(paymentAccounts)
        );

    }
}
