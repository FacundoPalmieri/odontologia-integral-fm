package com.odontologiaintegralfm.feature.consultation.catalogs.repository;


import com.odontologiaintegralfm.feature.consultation.catalogs.model.TreatmentCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ITreatmentConditionRepository extends JpaRepository<TreatmentCondition, Long> {

    Page<TreatmentCondition> findAll( Pageable pageable);
}
