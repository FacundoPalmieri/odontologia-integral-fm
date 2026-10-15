package com.odontologiaintegralfm.feature.consultation.core.prestationinstance.repository;

import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.model.PrestationInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import  com.odontologiaintegralfm.feature.consultation.core.prestationinstance.enums.PrestationInstanceStatus;

@Repository
public interface IPrestationInstanceRepository extends JpaRepository<PrestationInstance, Long> {

    /** Todas las prestaciones de un tratamiento, no solo las del request (D-28). */
    List<PrestationInstance> findByTreatmentInstanceId(Long treatmentInstanceId);

    /** Prestaciones IN_PROGRESS del paciente (D-37). */
    @Query("""
     SELECT pi
     FROM PrestationInstance pi
     WHERE pi.patient.id = :patientId
     AND pi.status = com.odontologiaintegralfm.feature.consultation.core.prestationinstance.enums.PrestationInstanceStatus.IN_PROGRESS
     """)
    List<PrestationInstance> findInProgressByPatientId(@Param("patientId") Long patientId);

    /** Devuelve vacío tanto si el id no existe como si es de otro paciente (D-60, ADR-0027). */
    Optional<PrestationInstance> findByIdAndPatientId(Long id, Long patientId);
}