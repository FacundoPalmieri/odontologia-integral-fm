package com.odontologiaintegralfm.feature.consultation.core.service.impl;


import com.odontologiaintegralfm.feature.consultation.core.model.ConsultationHistory;
import com.odontologiaintegralfm.feature.consultation.core.repository.IConsultationHistoryRepository;
import com.odontologiaintegralfm.feature.consultation.core.service.interfaces.IConsultationHistoryService;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConsultationHistoryService implements IConsultationHistoryService {

    private final IConsultationHistoryRepository consultationHistoryRepository;

    public ConsultationHistoryService(IConsultationHistoryRepository consultationHistoryRepository) {
        this.consultationHistoryRepository = consultationHistoryRepository;
    }




    /**
     * Persiste un historial de consulta.
     * @param consultationHistory : Objeto a crear
     */
    @Override
    public ConsultationHistory create(ConsultationHistory consultationHistory) {
        return consultationHistoryRepository.save(consultationHistory);
    }





    /**
     * Obtiene el historial de una consulta.
     *
     * @param id : id de la consulta.
     */
    @Override
    public ConsultationHistory get(Long id) {
        return consultationHistoryRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("exception.consultationHistory.notFound.user", null, "exception.consultationHistory.notFound.log", new Object[]{id, "ConsultationHistoryService", "getById"}, LogLevel.ERROR));
    }
}
