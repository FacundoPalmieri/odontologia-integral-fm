package com.odontologiaintegralfm.feature.consultation.core.service.interfaces;


import com.odontologiaintegralfm.feature.consultation.core.dto.ConsultationOdontogramCorrectionRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.dto.ConsultationOdontogramCreateRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.model.ConsultationOdontogramHeader;
import com.odontologiaintegralfm.shared.dto.Response;

import java.util.Optional;

public interface IConsultationOdontogramHeaderService {

    /**
     * Crea cabecera de odontograma
     * @param idConsultation : Id de la consulta
     * @param odontogram : Representación del odontograma + Observación.
     */
    Response<Void> create(Long idConsultation, ConsultationOdontogramCreateRequestDTO odontogram);


    /**
     * Actualiza cabecera de odontograma
     * @param idConsultation : Id de la consulta
     * @param correctionRequestDTO : Representación del odontograma + Observación.
     */
    Response<Void> update(Long idConsultation, ConsultationOdontogramCorrectionRequestDTO correctionRequestDTO);



    /**
     * Busca cabecera de un odontograma con estado "enabled = true"
     * @param idConsultation : id Consulta
     */
    Optional <ConsultationOdontogramHeader>  getById(Long idConsultation);
}
