package com.odontologiaintegralfm.repository;

import com.odontologiaintegralfm.model.HealthPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;


/**
 * @author [Facundo Palmieri]
 */
@Repository
public interface IHealthPlanRepository extends JpaRepository<HealthPlan, Long> {

    List<HealthPlan> findAllByEnabledTrue();

    Optional<HealthPlan> findByIdAndEnabledTrue(Long id);
}
