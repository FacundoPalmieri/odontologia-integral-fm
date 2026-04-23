package com.odontologiaintegralfm.feature.consultation.core.consultation.service;


import com.odontologiaintegralfm.feature.consultation.core.consultation.model.ConsultationHistory;
import com.odontologiaintegralfm.feature.consultation.core.consultation.repository.IConsultationHistoryRepository;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

@Service
public class ConsultationHistoryService {

    private final IConsultationHistoryRepository consultationHistoryRepository;

    public ConsultationHistoryService(IConsultationHistoryRepository consultationHistoryRepository) {
        this.consultationHistoryRepository = consultationHistoryRepository;
    }



    public ConsultationHistory create(ConsultationHistory consultationHistory) {
        try {
            return consultationHistoryRepository.save(consultationHistory);
        } catch (CannotCreateTransactionException | DataAccessException e) {
            throw new DataBaseException(e, "ConsultationHistoryService", consultationHistory.getId(), null, "create");
        }}




    public ConsultationHistory get(Long id) {
        return consultationHistoryRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("exception.consultationHistory.notFound.user", null, "exception.consultationHistory.notFound.log", new Object[]{id, "ConsultationHistoryService", "getById"}, LogLevel.ERROR));
    }
}
