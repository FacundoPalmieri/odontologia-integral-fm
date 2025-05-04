package com.odontologiaintegralfm.repository;

import com.odontologiaintegralfm.model.MedicalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IMedicalHistoryRepository extends JpaRepository<MedicalHistory, Long> {

}
