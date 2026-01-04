package com.odontologiaintegralfm.feature.consultation.core.service.interfaces;



import com.odontologiaintegralfm.feature.consultation.core.dto.ToothDTO;
import com.odontologiaintegralfm.feature.consultation.core.model.ConsultationOdontogramHeader;

public interface IConsultationOdontogramDetailsService {

    /**
     * Deshabilita detalles de un odontograma
     */
    void disable(ConsultationOdontogramHeader lastOdontogram);


    /**
     * Crea los detalles de un nuevo odontograma.
     */
    void create(ConsultationOdontogramHeader odontogramHeader, java.util.List<ToothDTO> odontogram);
}
