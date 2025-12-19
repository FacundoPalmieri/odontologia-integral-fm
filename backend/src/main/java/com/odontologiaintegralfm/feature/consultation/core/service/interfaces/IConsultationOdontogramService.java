package com.odontologiaintegralfm.feature.consultation.core.service.interfaces;


import com.odontologiaintegralfm.feature.consultation.core.dto.ToothDTO;
import com.odontologiaintegralfm.feature.consultation.core.model.ConsultationOdontogram;
import com.odontologiaintegralfm.shared.dto.Response;

import java.util.List;
import java.util.Optional;

public interface IConsultationOdontogramService {

    /**
     * Crea un odontograma
     * @param idConsultation : Id de la consulta
     * @param odontogram : Representación del odontograma.
     */
    Response<Void> createOdontogram(Long idConsultation, List<ToothDTO> odontogram);

    /**
     * Busca un odontograma con estado "enabled = true"
     * @param idConsultation : id Consulta
     */
    Optional <ConsultationOdontogram>  getById(Long idConsultation);
}
