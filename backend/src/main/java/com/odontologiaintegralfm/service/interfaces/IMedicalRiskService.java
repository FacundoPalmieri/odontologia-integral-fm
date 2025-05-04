package com.odontologiaintegralfm.service.interfaces;

import com.odontologiaintegralfm.dto.MedicalRiskResponseDTO;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.model.MedicalRisk;
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
     * Método para recuperar Set de Riesgos médicos "Habilitados" de acuerdo a los Ids recibidos.
     * @param ids de Riesgos médicos.
     * @return Set de objetos {@link MedicalRisk} habilitados.
     */
    Set<MedicalRisk> getByIds(Set<Long> ids);
}
