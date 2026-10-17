package com.odontologiaintegralfm.feature.consultation.prestationinstance.repository;

import com.odontologiaintegralfm.feature.consultation.prestationinstance.model.PrestationStepInstanceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IPrestationStepInstanceHistoryRepository extends JpaRepository<PrestationStepInstanceHistory, Long> {
}
