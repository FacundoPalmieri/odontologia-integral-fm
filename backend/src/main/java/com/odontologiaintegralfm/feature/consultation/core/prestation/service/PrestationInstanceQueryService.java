package com.odontologiaintegralfm.feature.consultation.core.prestation.service;


import com.odontologiaintegralfm.feature.consultation.core.prestation.model.PrestationInstance;
import com.odontologiaintegralfm.feature.consultation.core.prestation.repository.IPrestationInstanceRepository;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class PrestationInstanceQueryService {

    private final IPrestationInstanceRepository prestationInstanceRepository;

    public PrestationInstanceQueryService(IPrestationInstanceRepository prestationInstanceRepository) {
        this.prestationInstanceRepository = prestationInstanceRepository;
    }

    public PrestationInstance findById(Long id) {
        return prestationInstanceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("exception.prestationInstanceQueryService.notFound.user", null, "exception.prestationInstanceQueryService.notFound.log", new Object[]{id, "PrestationInstanceQueryService", "findById"}, LogLevel.ERROR));
    }
}