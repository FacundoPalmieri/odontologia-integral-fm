package com.odontologiaintegralfm.feature.consultation.paymentaccount.service;


import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.consultation.catalogs.model.PaymentProvider;
import com.odontologiaintegralfm.feature.consultation.catalogs.service.PaymentProviderService;
import com.odontologiaintegralfm.feature.consultation.paymentaccount.dto.PaymentAccountDTORequest;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UpdatePaymentAccountStateUseCase {

    private final IPaymentAccountRepository paymentAccountRepository;
    private final PaymentAccountMapper paymentAccountMapper;
    private final MessageSource messageSource;
    private final PaymentProviderService paymentProviderService;

    UpdatePaymentAccountStateUseCase(IPaymentAccountRepository paymentAccountRepository,
                                     PaymentAccountMapper paymentAccountMapper,
                                     @Qualifier("messageSource") MessageSource messageSource,
                                     PaymentProviderService paymentProviderService
    ) {
        this.paymentAccountRepository = paymentAccountRepository;
        this.paymentAccountMapper = paymentAccountMapper;
        this.messageSource = messageSource;
        this.paymentProviderService = paymentProviderService;
    }




    @Transactional
    public Response<PaymentAccountDTOResponse> execute(Long id, PaymentAccountDTORequest paymentAccountDTORequest) {

        //Recuperamos cuenta por ID
        PaymentAccount paymentAccount = paymentAccountRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("exception.paymentAccount.notfound.user",null,"exception.paymentAccount.notfound.log", new Object[]{id,"UpdatePaymentAccountStateUseCase", "execute"}, LogLevel.ERROR));





        //Validamos si cambio CBU y si lo cambió que no exista otra cuenta con el CBU que quiere actualizar
        if(!paymentAccount.getAccountIdentifier().equals(paymentAccountDTORequest.accountIdentifier())) {
            Optional<PaymentAccount> paymentAccountCbu = paymentAccountRepository.findByAccountIdentifier(paymentAccountDTORequest.accountIdentifier());
            if (paymentAccountCbu.isPresent() && !(paymentAccountCbu.get().getId().equals(paymentAccount.getId()))) {
                throw new ConflictException("exception.createPaymentAccountUseCase.accountDuplicateCbu.user", null, "exception.createPaymentAccountUseCase.accountDuplicateCbu.log", new Object[]{paymentAccountCbu.get().getAccountIdentifier(), paymentAccountCbu.get().getId(), "UpdatePaymentAccountStateUseCase", "execute"}, LogLevel.WARN);
            }
        }


        //Validamos que no exista otra cuenta con el Alias que quiere actualizar
        if(!paymentAccount.getAlias().equals(paymentAccountDTORequest.alias())) {
            Optional<PaymentAccount> paymentAccountAlias = paymentAccountRepository.findByAlias(paymentAccountDTORequest.alias());
            if (paymentAccountAlias.isPresent() && !(paymentAccountAlias.get().getId().equals(paymentAccount.getId()))) {
                throw new ConflictException("exception.createPaymentAccountUseCase.accountDuplicateAlias.user", null, "exception.createPaymentAccountUseCase.accountDuplicateAlias.log", new Object[]{paymentAccountAlias.get().getAccountIdentifier(), paymentAccountAlias.get().getId(), "UpdatePaymentAccountStateUseCase", "execute"}, LogLevel.WARN);
            }
        }

        //Buscamos el proveedor existente.
        PaymentProvider paymentProvider = null;
        if(!paymentAccount.getProvider().getId().equals(paymentAccountDTORequest.providerId())) {
             paymentProvider = paymentProviderService.getById(paymentAccountDTORequest.providerId());
        }
        //Mapeamos
        PaymentAccount paymentAccountUpdate = paymentAccountMapper.toEntity(paymentAccountDTORequest);
        paymentAccount.setProvider(
                (paymentProvider == null) ? paymentAccount.getProvider() : paymentProvider
        );

        //Persistimos
        PaymentAccount paymentAccountSaved = paymentAccountRepository.save(paymentAccount);

        //Armamos response
        PaymentAccountDTOResponse paymentAccountDTOResponse = paymentAccountMapper.toDTO(paymentAccountSaved);


        return new Response<>(
                true,
                messageSource.getMessage("paymentAccountUseCase.update.ok",null, LocaleContextHolder.getLocale()),
                paymentAccountDTOResponse
        );

    }





}
