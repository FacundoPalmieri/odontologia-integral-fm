package com.odontologiaintegralfm.service.interfaces;

import com.odontologiaintegralfm.dto.HealthPlanResponseDTO;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.model.HealthPlan;
import com.odontologiaintegralfm.model.Locality;

import java.util.List;

/**
 * Interfaz correspondiente a planes de salud.
 */
public interface IHealthPlanService {
    /**
     * Método para obtener todos los planes de salud "Habilitados"
     * @return HealthPlanResponseDTO
     */
    Response<List<HealthPlanResponseDTO>> getAll();

    /**
     * Método para obtener una "Prepaga habilitada" de acuerdo al ID recibido como parámetro.
     * @param id del tipo de Prepaga.
     * @return  {@link HealthPlan} con el tipo de Prepaga encontrada.
     */
    HealthPlan getById(Long id);
}
