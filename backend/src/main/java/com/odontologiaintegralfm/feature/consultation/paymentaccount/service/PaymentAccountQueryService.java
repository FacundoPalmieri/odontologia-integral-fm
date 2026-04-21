package com.odontologiaintegralfm.feature.consultation.paymentaccount.service;


import com.odontologiaintegralfm.feature.consultation.paymentaccount.repository.IPaymentAccountRepository;
import com.odontologiaintegralfm.feature.consultation.paymentaccount.dto.PaymentAccountDTOResponse;
import com.odontologiaintegralfm.feature.consultation.paymentaccount.mapper.PaymentAccountMapper;
import com.odontologiaintegralfm.feature.consultation.paymentaccount.model.PaymentAccount;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentAccountQueryService  {

    private final IPaymentAccountRepository paymentAccountRepository;
    private final PaymentAccountMapper paymentAccountMapper;

    public PaymentAccountQueryService(IPaymentAccountRepository paymentAccountRepository, PaymentAccountMapper paymentAccountMapper) {
        this.paymentAccountRepository = paymentAccountRepository;
        this.paymentAccountMapper = paymentAccountMapper;
    }


    public Response<PaymentAccountDTOResponse> getById(Long id) {

        PaymentAccount paymentAccount = paymentAccountRepository.findByIdAndEnabledTrue(id)
                .orElseThrow(()-> new NotFoundException("exception.paymentAccount.notfound.user",null,"exception.paymentAccount.notfound.log", new Object[]{id,"PaymentAccountQueryService", "getById"}, LogLevel.ERROR));


        return new Response<>(
                true,
                null,
                paymentAccountMapper.toDTO(paymentAccount)
        );
    }



    public Response<List<PaymentAccountDTOResponse>> getAll() {

        List<PaymentAccount> paymentAccounts = paymentAccountRepository.findAll();

        return new Response<>(
                true,
                null,
                paymentAccountMapper.toDTOList(paymentAccounts)
        );

    }


    public Response<List<PaymentAccountDTOResponse>> getAllEnabled() {
        List<PaymentAccount> paymentAccounts = paymentAccountRepository.findAllByEnabledTrue();
        return new Response<>(
                true,
                null,
                paymentAccountMapper.toDTOList(paymentAccounts)
        );

    }
}
