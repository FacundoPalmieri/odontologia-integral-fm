package com.odontologiaintegralfm.feature.consultation.core.service.interfaces;



import com.odontologiaintegralfm.feature.consultation.core.dto.ToothRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.model.OdontogramDetail;
import com.odontologiaintegralfm.feature.consultation.core.model.OdontogramHeader;

import java.util.List;

public interface IOdontogramDetailsService {

    /**
     * Deshabilita detalles de un odontograma
     */
    void disable(OdontogramHeader lastOdontogram);


    /**
     * Crea los detalles de un nuevo odontograma.
     */
    void create(OdontogramHeader odontogramHeader, java.util.List<ToothRequestDTO> odontogram);


    /**
     * Obtiene los detalles de un odontograma vigente, por ID de encabezado.
     */

    List<OdontogramDetail> getByIdHeader(Long idOdontogramHeader);
}

