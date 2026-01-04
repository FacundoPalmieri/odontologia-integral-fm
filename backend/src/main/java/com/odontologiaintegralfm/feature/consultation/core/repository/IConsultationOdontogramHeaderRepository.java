package com.odontologiaintegralfm.feature.consultation.core.repository;


import com.odontologiaintegralfm.feature.consultation.core.model.ConsultationOdontogramHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IConsultationOdontogramHeaderRepository extends JpaRepository<ConsultationOdontogramHeader, Long> {

    List<ConsultationOdontogramHeader> findAllByConsultationId(Long idConsultation);
}
