package com.odontologiaintegralfm.feature.consultation.core.consultationinstance.service;

import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.model.ConsultationInstance;
import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.repository.IConsultationInstanceRepository;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ConsultationInstanceQueryService {

    private final IConsultationInstanceRepository consultationInstanceRepository;

    public ConsultationInstanceQueryService(IConsultationInstanceRepository consultationInstanceRepository) {
        this.consultationInstanceRepository = consultationInstanceRepository;
    }

    public ConsultationInstance findById(Long id) {
        return consultationInstanceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "exception.getConsultationInstanceUseCase.notFound.user", null,
                        "exception.getConsultationInstanceUseCase.notFound.log",
                        new Object[]{id, "ConsultationInstanceQueryService", "findById"},
                        LogLevel.WARN
                ));
    }
}