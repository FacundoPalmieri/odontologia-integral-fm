package com.odontologiaintegralfm.service.interfaces;

import com.odontologiaintegralfm.dto.HealthPlanResponseDTO;
import com.odontologiaintegralfm.dto.Response;
import java.util.List;

/**
 * Interfaz correspondiente a planes de salud.
 */
public interface IHealthPlanService {
    /**
     * Método para obtener todos los planes de salud.
     * @return HealthPlanResponseDTO
     */
    Response<List<HealthPlanResponseDTO>> getAll();
}
