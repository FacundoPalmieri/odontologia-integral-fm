package com.odontologiaintegralfm.feature.patient.core.repository;

import com.odontologiaintegralfm.feature.patient.core.model.PatientMedicalRisk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * @author [Facundo Palmieri]
 */
@Repository
public interface IPatientMedicalRiskRepository extends JpaRepository<PatientMedicalRisk, Long> {

    Set<PatientMedicalRisk> findByPatientIdAndEnabledTrue(Long idPatient);

    Set<PatientMedicalRisk> findByPatientId(Long idPatient);

}
