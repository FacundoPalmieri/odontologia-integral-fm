package com.odontologiaintegralfm.feature.patient.catalogs.service.interfaces;

import com.odontologiaintegralfm.feature.patient.catalogs.dto.HealthPlanResponseDTO;
import com.odontologiaintegralfm.shared.response.Response;
import com.odontologiaintegralfm.feature.patient.catalogs.model.HealthPlan;

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
