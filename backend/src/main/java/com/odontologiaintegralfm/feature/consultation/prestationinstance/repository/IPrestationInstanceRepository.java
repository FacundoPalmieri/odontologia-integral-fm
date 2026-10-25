package com.odontologiaintegralfm.feature.consultation.prestationinstance.repository;

import com.odontologiaintegralfm.feature.consultation.prestationinstance.model.PrestationInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IPrestationInstanceRepository extends JpaRepository<PrestationInstance, Long> {

    /** Todas las prestaciones de un tratamiento, no solo las del request (D-28). */
    List<PrestationInstance> findByTreatmentInstanceId(Long treatmentInstanceId);

    /** Prestaciones IN_PROGRESS del paciente (D-37). */
    @Query("""
     SELECT pi
     FROM PrestationInstance pi
     WHERE pi.patient.id = :patientId
     AND pi.status = com.odontologiaintegralfm.feature.consultation.prestationinstance.enums.PrestationInstanceStatus.IN_PROGRESS
     """)
    List<PrestationInstance> findInProgressByPatientId(@Param("patientId") Long patientId);

    /** Devuelve vacío tanto si el id no existe como si es de otro paciente (D-60, ADR-0027). */
    Optional<PrestationInstance> findByIdAndPatientId(Long id, Long patientId);

    /** Prestaciones habilitadas del paciente, de cualquier estado (D-04, D-07). */
    List<PrestationInstance> findByPatientIdAndEnabledTrue(Long patientId);
}