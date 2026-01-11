package com.odontologiaintegralfm.feature.consultation.core.service.interfaces;


import com.odontologiaintegralfm.feature.consultation.core.dto.OdontogramCorrectionRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.dto.OdontogramCreateRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.dto.OdontogramResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.model.OdontogramHeader;
import com.odontologiaintegralfm.shared.dto.Response;

import java.util.Optional;

public interface IOdontogramHeaderService {

    /**
     * Crea cabecera de odontograma
     * @param idConsultation : Id de la consulta
     * @param odontogram : Representación del odontograma + Observación.
     */
    Response<Void> create(Long idConsultation, OdontogramCreateRequestDTO odontogram);


    /**
     * Actualiza cabecera de odontograma
     * @param idConsultation : Id de la consulta
     * @param correctionRequestDTO : Representación del odontograma + Observación.
     */
    Response<Void> update(Long idConsultation, OdontogramCorrectionRequestDTO correctionRequestDTO);



    /**
     * Método interno de la aplicación
     * Busca cabecera de un odontograma con estado "enabled = true"
     * @param idConsultation : id Consulta
     */
    Optional <OdontogramHeader> getByIdInternal(Long idConsultation);



    /**
     * Método que brinda respuesta al controlador.
     * Busca cabecera de un odontograma con estado "enabled = true"
     * @param idConsultation : id Consulta
     */
    Response<OdontogramResponseDTO> getById(Long idConsultation);
}
