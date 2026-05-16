package com.odontologiaintegralfm.feature.consultation.catalogs.prestation.repository;

import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.model.PrestationStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPrestationStepRepository extends JpaRepository<PrestationStep, Long> {

    List<PrestationStep> findByPrestationId(Long presstationId);
}
