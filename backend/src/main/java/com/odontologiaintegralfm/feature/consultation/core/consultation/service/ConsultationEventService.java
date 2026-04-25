package com.odontologiaintegralfm.feature.consultation.core.consultation.service;


import com.odontologiaintegralfm.feature.consultation.core.consultation.model.ConsultationEvent;
import com.odontologiaintegralfm.feature.consultation.core.consultation.repository.IConsultationEventRepository;
import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.enums.LogType;
import org.springframework.stereotype.Service;

@Service
public class ConsultationEventService{

    private final IConsultationEventRepository consultationEventRepository;

    public ConsultationEventService(IConsultationEventRepository consultationEventRepository) {
        this.consultationEventRepository = consultationEventRepository;
    }



    @LogAction(
            value = "consultationEventService.logAction.create.ok",
            args = {"#result.id", "#result.consultation.id", "#result.eventType"},
            type = LogType.SYSTEM,
            level = LogLevel.INFO

    )
    public ConsultationEvent create(ConsultationEvent consultationEvent) {
        return consultationEventRepository.save(consultationEvent);
    }
}
