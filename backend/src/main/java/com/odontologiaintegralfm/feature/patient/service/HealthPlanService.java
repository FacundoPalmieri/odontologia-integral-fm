package com.odontologiaintegralfm.feature.patient.service;

import com.odontologiaintegralfm.feature.patient.dto.HealthPlanResponseDTO;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import com.odontologiaintegralfm.feature.patient.model.HealthPlan;
import com.odontologiaintegralfm.feature.patient.repository.IHealthPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

import java.util.List;

/**
 * @author [Facundo Palmieri]
 */
@Service
public class HealthPlanService implements IHealthPlanService {

    @Autowired
    private IHealthPlanRepository healthPlanRepository;

    /**
     * Método para obtener todos los planes de salud "Habilitados".
     *
     * @return HealthPlanResponseDTO
     */
    @Override
    public Response<List<HealthPlanResponseDTO>> getAll() {
        try{
            List<HealthPlan> healthPlans= healthPlanRepository.findAllByEnabledTrue();
            List<HealthPlanResponseDTO> healthPlanResponseDTOs = healthPlans.stream()
                    .map(healthPlan -> new HealthPlanResponseDTO(healthPlan.getId(), healthPlan.getName()))
                    .toList();

            return new Response<>(true, null,healthPlanResponseDTOs);

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "HealthPlanService", 0L, "", "getAll");
        }
    }

    /**
     * Método para obtener una "Prepaga Habilitada" de acuerdo al ID recibido como parámetro.
     *
     * @param id del tipo de Prepaga.
     * @return {@link HealthPlan} con el tipo de Prepaga encontrada.
     */
    @Override
    public HealthPlan getById(Long id) {
        try{
            return healthPlanRepository.findByIdAndEnabledTrue(id).orElseThrow(()-> new NotFoundException("exception.healthPlanNotFound.user", null, "exception.healthPlanNotFound.log", new Object[]{ id,"HealthPlanService","getById"}, LogLevel.ERROR));
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "HealthPlanService", id, null, "getById");
        }
    }
}
