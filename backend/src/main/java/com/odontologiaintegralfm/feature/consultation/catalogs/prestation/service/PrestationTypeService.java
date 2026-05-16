package com.odontologiaintegralfm.feature.consultation.catalogs.prestation.service;

import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.model.PrestationType;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.repository.IPrestationTypeRepository;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class PrestationTypeService {

    private final IPrestationTypeRepository prestationTypeRepository;

    PrestationTypeService(IPrestationTypeRepository prestationTypeRepository) {
        this.prestationTypeRepository = prestationTypeRepository;
    }

    public PrestationType findById(Long id) {
        return prestationTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("exception.prestationTypeService.notFound.user",null,"exception.prestationTypeService.notFound.log", new Object[]{id, "prestationTypeService","findById"}, LogLevel.ERROR));
    }
}
