package com.odontologiaintegralfm.feature.consultation.core.repository;


import com.odontologiaintegralfm.feature.consultation.core.model.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IConsultationRepository extends JpaRepository<Consultation, Long> {
}
