package com.odontologiaintegralfm.feature.consultation.catalogs.service.implement;



import com.odontologiaintegralfm.feature.consultation.catalogs.dto.PaymentProviderDTOResponse;
import com.odontologiaintegralfm.feature.consultation.catalogs.mapper.PaymentProviderMapper;
import com.odontologiaintegralfm.feature.consultation.catalogs.model.PaymentProvider;
import com.odontologiaintegralfm.feature.consultation.catalogs.repository.IPaymentProviderRepository;
import com.odontologiaintegralfm.feature.consultation.catalogs.service.interfaces.IPaymentProviderService;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PaymentProviderService implements IPaymentProviderService {

    private final IPaymentProviderRepository paymentProviderRepository;
    private final PaymentProviderMapper mapper;

    public PaymentProviderService(IPaymentProviderRepository paymentProviderRepository, PaymentProviderMapper mapper) {
        this.paymentProviderRepository = paymentProviderRepository;
        this.mapper = mapper;
    }

    @Override
    public Response<List<PaymentProviderDTOResponse>> getAll() {
        List<PaymentProvider> paymentProviders = paymentProviderRepository.findAll();
        List<PaymentProviderDTOResponse> paymentProviderDTOResponses = mapper.toDTOList(paymentProviders);
        return new Response<>(true, "",paymentProviderDTOResponses);
    }

    /**Obtiene Proveedor por ID*/
    @Override
    public PaymentProvider getById(Long id) {
        return paymentProviderRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("exception.paymentProviderService.notFound.user",null,"exception.paymentProviderService.notFound.log",new Object[]{id,"PaymentProviderService","getById"}, LogLevel.ERROR));
    }


}
