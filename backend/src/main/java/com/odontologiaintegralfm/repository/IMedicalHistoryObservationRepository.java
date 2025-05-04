package com.odontologiaintegralfm.repository;

import com.odontologiaintegralfm.model.MedicalHistoryObservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IMedicalHistoryObservationRepository extends JpaRepository<MedicalHistoryObservation, Long> {
}
