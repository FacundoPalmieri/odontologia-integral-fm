package com.odontologiaintegralfm.feature.consultation.catalogs.prestation.service;


import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.model.PrestationStep;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.repository.IPrestationStepRepository;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrestationStepService {

    private final IPrestationStepRepository prestationStepRepository;

    PrestationStepService(IPrestationStepRepository prestationStepRepository) {
        this.prestationStepRepository = prestationStepRepository;
    }


    public List<PrestationStep> findAllPrestationStepsByPrestation(Long prestationId){
        return this.prestationStepRepository.findByPrestationId(prestationId);
    }



    public PrestationStep findById(Long id) {
        return prestationStepRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("exception.prestationStepService.notFound.user", null, "exception.prestationStepService.notFound.log", new Object[]{id, "PrestationStepService", "findById"}, LogLevel.ERROR));
    }




}
