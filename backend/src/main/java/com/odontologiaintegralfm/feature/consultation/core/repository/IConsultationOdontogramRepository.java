package com.odontologiaintegralfm.feature.consultation.core.repository;


import com.odontologiaintegralfm.feature.consultation.core.model.ConsultationOdontogram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface IConsultationOdontogramRepository extends JpaRepository<ConsultationOdontogram, Long> {

    List<ConsultationOdontogram> findByConsultationIdAndEnabledTrue(Long idConsultation);

    List<ConsultationOdontogram> findAllByConsultationId(Long idConsultation);
}
