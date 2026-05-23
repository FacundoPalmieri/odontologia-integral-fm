package com.odontologiaintegralfm.feature.consultation.core.consultation.service;


import com.odontologiaintegralfm.feature.consultation.core.consultation.model.ConsultationStatusHistory;
import com.odontologiaintegralfm.feature.consultation.core.consultation.repository.IConsultationStatusHistoryRepository;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

@Service
public class ConsultationStatusHistoryService {

    private final IConsultationStatusHistoryRepository consultationHistoryRepository;

    public ConsultationStatusHistoryService(IConsultationStatusHistoryRepository consultationHistoryRepository) {
        this.consultationHistoryRepository = consultationHistoryRepository;
    }



    public ConsultationStatusHistory create(ConsultationStatusHistory consultationStatusHistory) {
        try {
            return consultationHistoryRepository.save(consultationStatusHistory);
        } catch (CannotCreateTransactionException | DataAccessException e) {
            throw new DataBaseException(e, "ConsultationStatusHistoryService", consultationStatusHistory.getId(), null, "create");
        }}




    public ConsultationStatusHistory get(Long id) {
        return consultationHistoryRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("exception.consultationHistory.notFound.user", null, "exception.consultationHistory.notFound.log", new Object[]{id, "ConsultationStatusHistoryService", "getById"}, LogLevel.ERROR));
    }



}
