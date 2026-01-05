package com.odontologiaintegralfm.feature.consultation.core.repository;


import com.odontologiaintegralfm.feature.consultation.core.model.OdontogramDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface IOdontogramDetailRepository extends JpaRepository<OdontogramDetail, Long> {

    List<OdontogramDetail> findByOdontogramHeaderIdAndEnabledTrue(Long idOdontogramHeader);


}
