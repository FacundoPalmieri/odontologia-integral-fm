package com.odontologiaintegralfm.feature.consultation.core.prestationinstance.service;


import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.enums.PrestationStepStatus;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.model.PrestationInstance;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.model.PrestationStepInstance;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.repository.IPrestationStepInstanceRepository;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrestationStepInstanceQueryService {

    private final IPrestationStepInstanceRepository prestationStepInstanceRepository;

    public PrestationStepInstanceQueryService(IPrestationStepInstanceRepository  prestationStepInstanceRepository) {
        this.prestationStepInstanceRepository = prestationStepInstanceRepository;
    }

    public PrestationStepInstance findById(Long id) {
        return prestationStepInstanceRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("exception.prestationStepInstanceQueryService.notfound.user",null,"exception.prestationStepInstanceQueryService.notfound.log",new Object[]{id, "prestationStepInstanceQueryService","findById"}, LogLevel.ERROR));
    }


    public List<PrestationStepInstance> findByPrestationAndStepIdAndStatus(PrestationInstance prestationInstance, Long stepId, PrestationStepStatus prestationStepStatus) {
        return prestationStepInstanceRepository.findByPrestationAndStepId(prestationInstance, stepId, prestationStepStatus);
    }


}
