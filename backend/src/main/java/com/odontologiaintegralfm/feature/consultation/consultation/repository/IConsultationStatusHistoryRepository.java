package com.odontologiaintegralfm.feature.consultation.consultation.repository;

import com.odontologiaintegralfm.feature.consultation.consultation.model.ConsultationStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IConsultationStatusHistoryRepository extends JpaRepository <ConsultationStatusHistory, Long> {
}
