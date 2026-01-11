package com.odontologiaintegralfm.feature.consultation.core.service.interfaces;


import com.odontologiaintegralfm.feature.consultation.core.model.ConsultationHistory;

public interface IConsultationHistoryService {

    /**
     * Persiste un historial de consulta.
     * @param consultationHistory : Objeto a crear
     */
    ConsultationHistory create(ConsultationHistory consultationHistory);

    /**
     * Obtiene el historial de una consulta.
     * @param id : id de la consulta.
     */
    ConsultationHistory get(Long id);
}
