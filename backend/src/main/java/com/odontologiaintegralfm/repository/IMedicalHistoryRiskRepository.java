package com.odontologiaintegralfm.repository;

import com.odontologiaintegralfm.model.MedicalHistoryRisk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * @author [Facundo Palmieri]
 */
@Repository
public interface IMedicalHistoryRiskRepository extends JpaRepository<MedicalHistoryRisk, Long> {

    Set<MedicalHistoryRisk> findByMedicalHistoryIdAndEnabledTrue(Long idMedicalHistory);
}
