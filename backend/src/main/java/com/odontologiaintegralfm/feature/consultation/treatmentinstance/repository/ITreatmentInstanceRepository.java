package com.odontologiaintegralfm.feature.consultation.treatmentinstance.repository;

import com.odontologiaintegralfm.feature.consultation.treatmentinstance.model.TreatmentInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ITreatmentInstanceRepository extends JpaRepository<TreatmentInstance, Long> {

    /** Devuelve vacío tanto si el id no existe como si es de otro paciente (D-60, ADR-0027). */
    Optional<TreatmentInstance> findByIdAndPatientId(Long id, Long patientId);

    /** Tratamientos activos del paciente: REQUIRED o IN_PROGRESS. PREEXISTING nunca cuenta como activo (D-35, D-51). */
    @Query("""
     SELECT t
     FROM TreatmentInstance t
     WHERE t.patient.id = :patientId
     AND t.treatmentCondition IN (
         com.odontologiaintegralfm.feature.consultation.treatment.enums.TreatmentConditionType.REQUIRED,
         com.odontologiaintegralfm.feature.consultation.treatment.enums.TreatmentConditionType.IN_PROGRESS)
     """)
    List<TreatmentInstance> findActiveByPatientId(@Param("patientId") Long patientId);

    /** Tratamientos habilitados del paciente, de cualquier condición (D-07). */
    List<TreatmentInstance> findByPatientIdAndEnabledTrue(Long patientId);
}
