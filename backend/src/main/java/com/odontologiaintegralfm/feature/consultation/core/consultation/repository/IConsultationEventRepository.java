package com.odontologiaintegralfm.feature.consultation.core.consultation.repository;

import com.odontologiaintegralfm.feature.consultation.core.consultation.model.ConsultationEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IConsultationEventRepository extends JpaRepository<ConsultationEvent, Long> {
}
