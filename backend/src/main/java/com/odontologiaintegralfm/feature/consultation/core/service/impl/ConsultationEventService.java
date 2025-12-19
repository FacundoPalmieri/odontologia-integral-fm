package com.odontologiaintegralfm.feature.consultation.core.service.impl;


import com.odontologiaintegralfm.feature.consultation.core.model.ConsultationEvent;
import com.odontologiaintegralfm.feature.consultation.core.repository.IConsultationEventRepository;
import com.odontologiaintegralfm.feature.consultation.core.service.interfaces.IConsultationEventService;
import org.springframework.stereotype.Service;

@Service
public class ConsultationEventService implements IConsultationEventService {

    private final IConsultationEventRepository consultationEventRepository;

    public ConsultationEventService(IConsultationEventRepository consultationEventRepository) {
        this.consultationEventRepository = consultationEventRepository;
    }

    @Override
    public ConsultationEvent create(ConsultationEvent consultationEvent) {
        return consultationEventRepository.save(consultationEvent);
    }
}
