package com.odontologiaintegralfm.feature.consultation.core.prestationinstance.service;


import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.model.PrestationInstance;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.repository.IPrestationInstanceRepository;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PrestationInstanceQueryService {

    private final IPrestationInstanceRepository prestationInstanceRepository;

    public PrestationInstanceQueryService(IPrestationInstanceRepository prestationInstanceRepository) {
        this.prestationInstanceRepository = prestationInstanceRepository;
    }

    @Transactional(readOnly = true)
    public PrestationInstance findById(Long id) {
        return prestationInstanceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("exception.prestationInstanceQueryService.notFound.user", null, "exception.prestationInstanceQueryService.notFound.log", new Object[]{id, "PrestationInstanceQueryService", "findById"}, LogLevel.ERROR));
    }
}