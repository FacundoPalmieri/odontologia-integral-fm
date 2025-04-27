package com.odontologiaintegralfm.repository;

import com.odontologiaintegralfm.dto.HealthPlanResponseDTO;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.model.HealthPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;


/**
 * @author [Facundo Palmieri]
 */
@Repository
public interface IHealthPlanRepository extends JpaRepository<HealthPlan, Long> {

    List<HealthPlan> findAll();
}
