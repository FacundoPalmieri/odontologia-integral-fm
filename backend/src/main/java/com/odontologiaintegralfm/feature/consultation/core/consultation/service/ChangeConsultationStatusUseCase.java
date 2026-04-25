package com.odontologiaintegralfm.feature.consultation.core.consultation.service;

import com.odontologiaintegralfm.feature.consultation.core.consultation.mapper.ConsultationMapper;
import com.odontologiaintegralfm.feature.consultation.core.consultation.model.Consultation;
import com.odontologiaintegralfm.feature.consultation.core.consultation.model.ConsultationHistory;
import com.odontologiaintegralfm.feature.consultation.core.consultation.repository.IConsultationRepository;
import com.odontologiaintegralfm.feature.consultation.core.consultation.dto.ConsultationResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.consultation.enums.ConsultationStatusType;
import com.odontologiaintegralfm.infrastructure.websocket.service.WebSocketEventPublisher;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangeConsultationStatusUseCase {

    private final IConsultationRepository consultationRepository;
    private final ConsultationHistoryService consultationHistoryService;
    private final WebSocketEventPublisher webSocketEventPublisher;
    private final ConsultationMapper consultationMapper;


    ChangeConsultationStatusUseCase(IConsultationRepository consultationRepository,
                                    ConsultationHistoryService consultationHistoryService,
                                    WebSocketEventPublisher webSocketEventPublisher,
                                    ConsultationMapper consultationMapper) {
        this.consultationRepository = consultationRepository;
        this.consultationHistoryService = consultationHistoryService;
        this.webSocketEventPublisher = webSocketEventPublisher;
        this.consultationMapper = consultationMapper;
    }

    @Transactional
    public ConsultationResponseDTO execute(Consultation consultation, ConsultationStatusType newStatus) {
        Consultation saved = persist(consultation, newStatus);
        createHistory(saved, newStatus);
        return buildAndPublish(saved);
    }

    private Consultation persist(Consultation consultation, ConsultationStatusType newStatus) {
        consultation.setStatus(newStatus);
        try {
            return consultationRepository.save(consultation);
        } catch (CannotCreateTransactionException | DataAccessException e) {
            throw new DataBaseException(e, "ChangeConsultationStatusUseCase", consultation.getId(), null, "persist");
        }
    }


    private void createHistory(Consultation saved, ConsultationStatusType newStatus) {
        consultationHistoryService.create(
                ConsultationHistory.build(saved, newStatus)
        );
    }

    private ConsultationResponseDTO buildAndPublish(Consultation saved) {
        ConsultationResponseDTO dto = consultationMapper.toDTO(saved);
        webSocketEventPublisher.publish(saved.getStatus().webSocketEvent(), dto);
        return dto;
    }
}