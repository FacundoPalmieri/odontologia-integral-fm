package com.odontologiaintegralfm.feature.consultation.core.treatmentinstance.repository;

import com.odontologiaintegralfm.feature.consultation.core.treatmentinstance.model.TreatmentInstanceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ITreatmentInstanceHistoryRepository extends JpaRepository<TreatmentInstanceHistory, Long> {
}
