package com.odontologiaintegralfm.feature.consultation.paymentaccount.service.impl;


import com.odontologiaintegralfm.feature.consultation.catalogs.model.PaymentProvider;
import com.odontologiaintegralfm.feature.consultation.paymentaccount.repository.IPaymentAccountRepository;
import com.odontologiaintegralfm.feature.consultation.catalogs.service.interfaces.IPaymentProviderService;
import com.odontologiaintegralfm.feature.consultation.paymentaccount.dto.PaymentAccountDTORequest;
import com.odontologiaintegralfm.feature.consultation.paymentaccount.dto.PaymentAccountDTOResponse;
import com.odontologiaintegralfm.feature.consultation.paymentaccount.mapper.PaymentAccountMapper;
import com.odontologiaintegralfm.feature.consultation.paymentaccount.model.PaymentAccount;
import com.odontologiaintegralfm.feature.consultation.paymentaccount.service.ICreatePaymentAccountUseCase;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class CreatePaymentAccountUseCase implements ICreatePaymentAccountUseCase {

    private final IPaymentAccountRepository paymentAccountRepository;
    private final PaymentAccountMapper paymentAccountMapper;
    private final MessageSource messageSource;
    private final IPaymentProviderService paymentProviderService;

    public CreatePaymentAccountUseCase(IPaymentAccountRepository paymentAccountRepository, PaymentAccountMapper paymentAccountMapper, @Qualifier("messageSource") MessageSource messageSource, IPaymentProviderService paymentProviderService) {
        this.paymentAccountRepository = paymentAccountRepository;
        this.paymentAccountMapper = paymentAccountMapper;
        this.messageSource = messageSource;
        this.paymentProviderService = paymentProviderService;

    }

    /**
     * Crea una cuenta para cobros
     */
    @Override
    @Transactional
    public Response<PaymentAccountDTOResponse> execute(PaymentAccountDTORequest paymentAccountDTORequest) {

        //Validamos que la cuenta no exista.
        Optional<PaymentAccount> paymentAccountCbu = paymentAccountRepository.findByAccountIdentifier(paymentAccountDTORequest.accountIdentifier());
        if(paymentAccountCbu.isPresent()) {
            throw new ConflictException("exception.createPaymentAccountUseCase.accountDuplicateCbu.user",null,"exception.createPaymentAccountUseCase.accountDuplicateCbu.log",new Object[]{paymentAccountCbu.get().getAccountIdentifier(),paymentAccountCbu.get().getId(),"paymentAccountUseCase","create"}, LogLevel.WARN);
        }

        Optional<PaymentAccount> paymentAccountAlias = paymentAccountRepository.findByAlias(paymentAccountDTORequest.alias());
        if(paymentAccountAlias.isPresent()) {
            throw new ConflictException("exception.createPaymentAccountUseCase.accountDuplicateAlias.user",null,"exception.createPaymentAccountUseCase.accountDuplicateAlias.log",new Object[]{paymentAccountAlias.get().getAccountIdentifier(),paymentAccountAlias.get().getId(),"paymentAccountUseCase","create"}, LogLevel.WARN);
        }

        //Buscamos el proveedor existente.
        PaymentProvider paymentProvider = paymentProviderService.getById(paymentAccountDTORequest.providerId());


        //Mapeamos
        PaymentAccount paymentAccount = paymentAccountMapper.toEntity(paymentAccountDTORequest);
        paymentAccount.setProvider(paymentProvider);


        //Persistimos
        PaymentAccount paymentAccountSaved = paymentAccountRepository.save(paymentAccount);

        //Armamos response
        PaymentAccountDTOResponse paymentAccountDTOResponse = paymentAccountMapper.toDTO(paymentAccountSaved);


        return new Response<>(
                true,
                messageSource.getMessage("paymentAccountUseCase.create.ok",null, LocaleContextHolder.getLocale()),
                paymentAccountDTOResponse
        );

    }
}
