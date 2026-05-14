package com.odontologiaintegralfm.feature.consultation.core.prestation.repository;

import com.odontologiaintegralfm.feature.consultation.core.prestation.enums.PrestationInstanceStatus;
import com.odontologiaintegralfm.feature.consultation.core.prestation.model.PrestationInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPrestationInstanceRepository extends JpaRepository<PrestationInstance, Long> {

    @Query("""
     SELECT pi
     FROM PrestationInstance pi
     WHERE pi.consultationInstance.consultation.appointment.patient.id = :patientId
     AND pi.status = :status
     """)
    List<PrestationInstance> findPrestationInstanceByPatientAndStatus(@Param("patientId")Long patientId,
                                                                      @Param("status") PrestationInstanceStatus status
    );
}