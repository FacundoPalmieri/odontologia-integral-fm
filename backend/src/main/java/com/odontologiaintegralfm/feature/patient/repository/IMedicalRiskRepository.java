package com.odontologiaintegralfm.feature.patient.repository;

import com.odontologiaintegralfm.feature.patient.model.MedicalRisk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface IMedicalRiskRepository extends JpaRepository<MedicalRisk, Long> {

    Set<MedicalRisk> findAllByEnabledTrue();

    MedicalRisk findByIdAndEnabledTrue(Long id);
}
