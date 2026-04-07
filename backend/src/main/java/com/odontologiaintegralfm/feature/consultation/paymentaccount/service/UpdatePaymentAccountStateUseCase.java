package com.odontologiaintegralfm.feature.consultation.paymentaccount.service;


import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.consultation.paymentaccount.dto.PaymentAccountDTOResponse;
import com.odontologiaintegralfm.feature.consultation.paymentaccount.mapper.PaymentAccountMapper;
import com.odontologiaintegralfm.feature.consultation.paymentaccount.model.PaymentAccount;
import com.odontologiaintegralfm.feature.consultation.paymentaccount.repository.IPaymentAccountRepository;
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


    /**
     * @param idAccount: id Cuenta
     */
    @Override
    public Response<PaymentAccountDTOResponse> disabled(Long idAccount) {

        //Recuperamos cuenta por ID
        PaymentAccount paymentAccount = paymentAccountRepository.findById(idAccount)
                .orElseThrow(()-> new NotFoundException("exception.paymentAccount.notfound.user",null,"exception.paymentAccount.notfound.log", new Object[]{idAccount,"UpdatePaymentAccountStateUseCase", "disabled"}, LogLevel.ERROR));


        //Validamos que no esté deshabilitada
        if(!paymentAccount.getEnabled()){
            throw new ConflictException("exception.paymentAccount.disabled.user",null,"exception.paymentAccount.disabled.log", new Object[]{idAccount,"UpdatePaymentAccountStateUseCase", "disabled"}, LogLevel.ERROR);
        }

        //Actualizamos valor
        paymentAccount.disable(authenticatedUserService.getAuthenticatedUser());

        //persiste
        PaymentAccount paymentAccountSaved = paymentAccountRepository.save(paymentAccount);

        //Response
        return new Response<>(
                true,
                messageSource.getMessage("paymentAccount.disabled.ok", null, LocaleContextHolder.getLocale()),
                paymentAccountMapper.toDTO(paymentAccountSaved)
        );
    }



    /**
     * @param idAccount: id Cuenta
     */
    @Override
    public Response<PaymentAccountDTOResponse> enabled(Long idAccount) {

        //Recuperamos cuenta por ID
        PaymentAccount paymentAccount = paymentAccountRepository.findById(idAccount)
                .orElseThrow(()-> new NotFoundException("exception.paymentAccount.notfound.user",null,"exception.paymentAccount.notfound.log", new Object[]{idAccount,"UpdatePaymentAccountStateUseCase", "disabled"}, LogLevel.ERROR));


        //Validamos que no esté habilitada
        if(paymentAccount.getEnabled()){
            throw new ConflictException("exception.paymentAccount.enabled.user",null,"exception.paymentAccount.enabled.log", new Object[]{idAccount,"UpdatePaymentAccountStateUseCase", "disabled"}, LogLevel.ERROR);
        }

        //Actualizamos valor
        paymentAccount.enable();

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
