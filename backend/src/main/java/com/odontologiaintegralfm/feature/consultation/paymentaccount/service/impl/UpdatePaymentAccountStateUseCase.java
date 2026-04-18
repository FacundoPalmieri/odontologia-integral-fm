package com.odontologiaintegralfm.feature.consultation.paymentaccount.service.impl;


import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.consultation.paymentaccount.dto.PaymentAccountDTOResponse;
import com.odontologiaintegralfm.feature.consultation.paymentaccount.mapper.PaymentAccountMapper;
import com.odontologiaintegralfm.feature.consultation.paymentaccount.model.PaymentAccount;
import com.odontologiaintegralfm.feature.consultation.paymentaccount.repository.IPaymentAccountRepository;
import com.odontologiaintegralfm.feature.consultation.paymentaccount.service.IUpdatePaymentAccountStateUseCase;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UpdatePaymentAccountStateUseCase implements IUpdatePaymentAccountStateUseCase {

    private final IPaymentAccountRepository paymentAccountRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final PaymentAccountMapper paymentAccountMapper;
    private final MessageSource messageSource;

    UpdatePaymentAccountStateUseCase(IPaymentAccountRepository paymentAccountRepository, AuthenticatedUserService authenticatedUserService, PaymentAccountMapper paymentAccountMapper, @Qualifier("messageSource") MessageSource messageSource) {
        this.paymentAccountRepository = paymentAccountRepository;
        this.authenticatedUserService = authenticatedUserService;
        this.paymentAccountMapper = paymentAccountMapper;
        this.messageSource = messageSource;
    }





}
