package com.odontologiaintegralfm.feature.patient.catalogs.repository;

import com.odontologiaintegralfm.feature.patient.catalogs.model.HealthPlan;
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
