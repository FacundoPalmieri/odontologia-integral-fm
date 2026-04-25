package com.odontologiaintegralfm.feature.consultation.core.paymentaccount.service;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.consultation.core.paymentaccount.dto.PaymentAccountDTOResponse;
import com.odontologiaintegralfm.feature.consultation.core.paymentaccount.mapper.PaymentAccountMapper;
import com.odontologiaintegralfm.feature.consultation.core.paymentaccount.model.PaymentAccount;
import com.odontologiaintegralfm.feature.consultation.core.paymentaccount.repository.IPaymentAccountRepository;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivatePaymentAccountUseCase{

    private final IPaymentAccountRepository paymentAccountRepository;
    private final PaymentAccountMapper paymentAccountMapper;
    private final MessageSource messageSource;
    private final AuthenticatedUserService authenticatedUserService;

    ActivatePaymentAccountUseCase(IPaymentAccountRepository paymentAccountRepository, PaymentAccountMapper paymentAccountMapper, @Qualifier("messageSource") MessageSource messageSource,AuthenticatedUserService authenticatedUserService) {
        this.paymentAccountRepository = paymentAccountRepository;
        this.paymentAccountMapper = paymentAccountMapper;
        this.messageSource = messageSource;
        this.authenticatedUserService = authenticatedUserService;
    }



    @Transactional
    public Response<PaymentAccountDTOResponse> execute(Long idAccount) {

        //Recuperamos cuenta por ID
        PaymentAccount paymentAccount = paymentAccountRepository.findById(idAccount)
                .orElseThrow(()-> new NotFoundException("exception.paymentAccount.notfound.user",null,"exception.paymentAccount.notfound.log", new Object[]{idAccount,"UpdatePaymentAccountUseCase", "disabled"}, LogLevel.ERROR));


        //Validamos que no esté habilitada
        if(paymentAccount.getEnabled()){
            throw new ConflictException("exception.paymentAccount.enabled.user",null,"exception.paymentAccount.enabled.log", new Object[]{idAccount,"UpdatePaymentAccountUseCase", "disabled"}, LogLevel.ERROR);
        }

        //Actualizamos estado
        paymentAccount.enable(authenticatedUserService.getAuthenticatedUser());

        //persiste
        PaymentAccount paymentAccountSaved = paymentAccountRepository.save(paymentAccount);

        PaymentAccountDTOResponse paymentAccountDTOResponse = paymentAccountMapper.toDTO(paymentAccountSaved);

        //Response
        return new Response<>(
                true,
                messageSource.getMessage("paymentAccount.enabled.ok", null, LocaleContextHolder.getLocale()),
                paymentAccountDTOResponse
        );
    }
}
