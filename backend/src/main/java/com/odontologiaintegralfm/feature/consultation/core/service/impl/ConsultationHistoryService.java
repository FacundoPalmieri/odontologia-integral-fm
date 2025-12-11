package com.odontologiaintegralfm.feature.consultation.core.service.impl;


import com.odontologiaintegralfm.feature.consultation.core.model.ConsultationHistory;
import com.odontologiaintegralfm.feature.consultation.core.repository.IConsultationHistoryRepository;
import com.odontologiaintegralfm.feature.consultation.core.service.interfaces.IConsultationHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConsultationHistoryService implements IConsultationHistoryService {

    @Autowired
    private IConsultationHistoryRepository repository;

    @Override
    public ConsultationHistory create(ConsultationHistory consultationHistory) {
        return repository.save(consultationHistory);
    }
}
