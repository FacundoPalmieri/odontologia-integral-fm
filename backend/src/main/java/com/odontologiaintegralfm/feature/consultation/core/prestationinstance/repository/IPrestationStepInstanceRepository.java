package com.odontologiaintegralfm.feature.consultation.core.prestationinstance.repository;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.enums.PrestationStepStatus;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.model.PrestationInstance;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.model.PrestationStepInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IPrestationStepInstanceRepository extends JpaRepository<PrestationStepInstance, Long> {

    @Query("""
     SELECT psi
     FROM PrestationStepInstance psi
     WHERE psi.prestationInstance = :prestationInstance
     AND psi.step.id = :stepId
     AND psi.status = :status
     """)
    List<PrestationStepInstance> findByPrestationAndStepId(@Param("prestationInstance")PrestationInstance prestationInstance,
                                                           @Param("stepId") Long stepId,
                                                           @Param("status")PrestationStepStatus prestationStepStatus
    );

    List<PrestationStepInstance> findByPrestationInstance(PrestationInstance prestationInstance);

    List<PrestationStepInstance> findByPrestationInstanceIn(List<PrestationInstance> prestationInstances);

    /** Devuelve vacío si el step no existe o es de otra prestación (D-60, ADR-0027). */
    Optional<PrestationStepInstance> findByIdAndPrestationInstanceId(Long id, Long prestationInstanceId);
}
