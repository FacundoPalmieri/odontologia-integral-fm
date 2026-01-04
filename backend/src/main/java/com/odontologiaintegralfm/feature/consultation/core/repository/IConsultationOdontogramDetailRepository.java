package com.odontologiaintegralfm.feature.consultation.core.repository;


import com.odontologiaintegralfm.feature.consultation.core.model.ConsultationOdontogramDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface IConsultationOdontogramDetailRepository extends JpaRepository<ConsultationOdontogramDetail, Long> {

    List<ConsultationOdontogramDetail> findByOdontogramHeaderIdAndEnabledTrue(Long idOdontogramHeader);


}
