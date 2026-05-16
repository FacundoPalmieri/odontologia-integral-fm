package com.odontologiaintegralfm.feature.payment.catalogs.paymentprovider.service;



import com.odontologiaintegralfm.feature.payment.catalogs.paymentprovider.dto.PaymentProviderDTOResponse;
import com.odontologiaintegralfm.feature.payment.catalogs.paymentprovider.mapper.PaymentProviderMapper;
import com.odontologiaintegralfm.feature.payment.catalogs.paymentprovider.model.PaymentProvider;
import com.odontologiaintegralfm.feature.payment.catalogs.paymentprovider.repository.IPaymentProviderRepository;
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




    //------------------ BÚSQUEDAS ------------------------------------//

    //------------------ Métodos Cliente-------------------------------//

    public Response<List<PaymentProviderDTOResponse>> getAll() {
        List<PaymentProvider> paymentProviders = paymentProviderRepository.findAllByEnabledTrue();
        List<PaymentProviderDTOResponse> paymentProviderDTOResponses = mapper.toDTOList(paymentProviders);
        return new Response<>(true, "",paymentProviderDTOResponses);
    }




    //------------------ Métodos interno -------------------------------//
    /**Obtiene Proveedor por ID*/
    public PaymentProvider findById(Long id) {
        return paymentProviderRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("exception.paymentProviderService.notFound.user",null,"exception.paymentProviderService.notFound.log",new Object[]{id,"PaymentProviderService","getById"}, LogLevel.ERROR));
    }


}
