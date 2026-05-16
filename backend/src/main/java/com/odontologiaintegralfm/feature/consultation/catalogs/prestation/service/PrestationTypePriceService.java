package com.odontologiaintegralfm.feature.consultation.catalogs.prestation.service;

import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.model.PrestationTypePrice;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.repository.IPrestationTypePriceRepository;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import org.springframework.stereotype.Service;

@Service
public class PrestationTypePriceService {

    private IPrestationTypePriceRepository  prestationTypePriceRepository;

    PrestationTypePriceService(IPrestationTypePriceRepository prestationTypePriceRepository) {
        this.prestationTypePriceRepository = prestationTypePriceRepository;
    }


    public PrestationTypePrice currentPrice(Long prestationTypeId) {
        return prestationTypePriceRepository.currentPrice(prestationTypeId)
                 .orElseThrow(() -> new ConflictException("exception.currentPrice.user", new Object[]{prestationTypeId}, "exception.currentPrice.log", new Object[]{prestationTypeId,"PrestationTypePriceService","currentPrice"}, LogLevel.ERROR));
    }
}
