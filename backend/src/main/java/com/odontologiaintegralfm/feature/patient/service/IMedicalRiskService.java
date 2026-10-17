package com.odontologiaintegralfm.feature.patient.service;

import com.odontologiaintegralfm.feature.patient.dto.MedicalRiskResponseDTO;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.feature.patient.model.MedicalRisk;
import java.util.Set;

/**
 * @author [Facundo Palmieri]
 */
public interface IMedicalRiskService {

    /**
     * Método para recuperar Set de todos los Riesgos médicos "Habilitados".
     * @return Set de objetos {@link MedicalRisk} habilitados.
     */
    Response<Set<MedicalRiskResponseDTO>> getAll();


    /**
     * Método para recuperar  Riesgos médicos "Habilitados" de acuerdo a los Ids recibidos.
     *
     * @param id de Riesgos médicos.
     * @return Set de objetos {@link MedicalRisk} habilitados.
     */
    MedicalRisk getById(Long id);


}
