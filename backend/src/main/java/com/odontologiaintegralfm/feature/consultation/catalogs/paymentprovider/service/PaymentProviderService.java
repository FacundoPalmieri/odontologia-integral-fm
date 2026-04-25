package com.odontologiaintegralfm.feature.consultation.catalogs.paymentprovider.service;



import com.odontologiaintegralfm.feature.consultation.catalogs.paymentprovider.dto.PaymentProviderDTOResponse;
import com.odontologiaintegralfm.feature.consultation.catalogs.paymentprovider.mapper.PaymentProviderMapper;
import com.odontologiaintegralfm.feature.consultation.catalogs.paymentprovider.model.PaymentProvider;
import com.odontologiaintegralfm.feature.consultation.catalogs.paymentprovider.repository.IPaymentProviderRepository;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PaymentProviderService  {

    private final IPaymentProviderRepository paymentProviderRepository;
    private final PaymentProviderMapper mapper;

    public PaymentProviderService(IPaymentProviderRepository paymentProviderRepository, PaymentProviderMapper mapper) {
        this.paymentProviderRepository = paymentProviderRepository;
        this.mapper = mapper;
    }

    public Response<List<PaymentProviderDTOResponse>> getAll() {
        List<PaymentProvider> paymentProviders = paymentProviderRepository.findAllByEnabledTrue();
        List<PaymentProviderDTOResponse> paymentProviderDTOResponses = mapper.toDTOList(paymentProviders);
        return new Response<>(true, "",paymentProviderDTOResponses);
    }

    /**Obtiene Proveedor por ID*/
    public PaymentProvider getById(Long id) {
        return paymentProviderRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("exception.paymentProviderService.notFound.user",null,"exception.paymentProviderService.notFound.log",new Object[]{id,"PaymentProviderService","getById"}, LogLevel.ERROR));
    }


}
