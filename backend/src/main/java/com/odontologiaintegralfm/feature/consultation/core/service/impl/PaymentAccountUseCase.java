package com.odontologiaintegralfm.feature.consultation.core.service.impl;


import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.consultation.catalogs.mapper.PaymentProviderMapper;
import com.odontologiaintegralfm.feature.consultation.core.dto.PaymentAccountDTORequest;
import com.odontologiaintegralfm.feature.consultation.core.dto.PaymentAccountDTOResponse;
import com.odontologiaintegralfm.feature.consultation.core.mapper.PaymentAccountMapper;
import com.odontologiaintegralfm.feature.consultation.core.model.PaymentAccount;
import com.odontologiaintegralfm.feature.consultation.catalogs.model.PaymentProvider;
import com.odontologiaintegralfm.feature.consultation.core.repository.IPaymentAccountRepository;
import com.odontologiaintegralfm.feature.consultation.core.service.interfaces.IPaymentAccountUseCase;
import com.odontologiaintegralfm.feature.consultation.catalogs.service.interfaces.IPaymentProviderService;
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
@Transactional
public class PaymentAccountUseCase implements IPaymentAccountUseCase {

    private final IPaymentAccountRepository paymentAccountRepository;
    private final PaymentAccountMapper paymentAccountMapper;
    private final MessageSource messageSource;
    private final IPaymentProviderService paymentProviderService;

    public PaymentAccountUseCase(IPaymentAccountRepository paymentAccountRepository, PaymentAccountMapper paymentAccountMapper, @Qualifier("messageSource") MessageSource messageSource, IPaymentProviderService paymentProviderService) {
        this.paymentAccountRepository = paymentAccountRepository;
        this.paymentAccountMapper = paymentAccountMapper;
        this.messageSource = messageSource;
        this.paymentProviderService = paymentProviderService;

    }

    /**
     * Crea una cuenta para cobros
     */
    @Override
    public Response<PaymentAccountDTOResponse> execute(PaymentAccountDTORequest paymentAccountDTORequest) {

        //Validamos que la cuenta no exista.
        Optional<PaymentAccount> paymentAccountExisting = paymentAccountRepository.findByAccountIdentifier(paymentAccountDTORequest.accountIdentifier());
        if(paymentAccountExisting.isPresent()) {
            throw new ConflictException("exception.paymentAccountUseCase.accountDuplicate.user",null,"exception.paymentAccountUseCase.accountDuplicate.log",new Object[]{paymentAccountExisting.get().getAccountIdentifier(),paymentAccountExisting.get().getId(),"paymentAccountUseCase","create"}, LogLevel.WARN);
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
